package com.library.steps;


import com.library.pages.BookPage;
import com.library.utility.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class APIStepDefs {
    public Logger LOG = LogManager.getLogger();

    RequestSpecification givenPart = RestAssured.given().log().uri();
    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;


    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role) {
        givenPart.header("x-library-token", LibraryAPI_Util.getToken(role));
        LOG.info("Token is generated as " + role);
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);

    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {
        LOG.info("Endpoint ---> " + endpoint);

        response = givenPart.when().get(endpoint);

        thenPart = response.then();

        jp = response.jsonPath();

        LOG.info("Response --> " + response.prettyPrint());
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        // OPT - 1
        Assert.assertEquals(expectedStatusCode, response.statusCode());

        // OPT - 2
        thenPart.statusCode(expectedStatusCode);

        LOG.info("Status Code --> " + response.statusCode());

    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {
        // OPT - 1
        Assert.assertEquals(expectedContentType, response.contentType());

        // OPT - 2
        thenPart.contentType(expectedContentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String path) {

        // OPT - 1
        thenPart.body(path, Matchers.everyItem(Matchers.notNullValue()));

        // OPT - 2
        List<String> allData = jp.getList(path);
        for (String eachData : allData) {
            Assert.assertNotNull(eachData);
        }
    }

    /**
     * US02
     */
    String expectedID;

    @Given("Path param {string} is {string}")
    public void path_param_is(String pathParam, String value) {
        givenPart.pathParam(pathParam, value);
        expectedID = value;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {
        String actualID = jp.getString(path);
        Assert.assertEquals(expectedID, actualID);
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> allPaths) {

        for (String eachPath : allPaths) {
            thenPart.body(eachPath, Matchers.notNullValue());
        }


    }

    /**
     * US03
     */

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String contentType) {
        givenPart.contentType(contentType);
    }

    Map<String, Object> randomData=new HashMap<>();
    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String dataType) {


        switch (dataType) {
            case "book":
                randomData= LibraryAPI_Util.getRandomBookMap();
                break;
            case "user":
                randomData = LibraryAPI_Util.getRandomUserMap();
                break;
            default:
                LOG.error("Invalid Data Type " + dataType);
                throw new RuntimeException("Invalid Data Type " + dataType);
        }
        LOG.info(dataType+" body is "+randomData);
        givenPart.formParams(randomData);

    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endpoint) {
         response = givenPart.when().post(endpoint);
         thenPart = response.then();
         jp=response.jsonPath();

         LOG.info("Response is "+response.prettyPrint());
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String expectedMessage) {
        String actualMessage = jp.getString(path);
        LOG.info("Actual Message is "+actualMessage);

        Assert.assertEquals(expectedMessage,actualMessage);
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {
        // OPT 1
        thenPart.body(path,Matchers.notNullValue());

        // OPT 2
        Assert.assertNotNull(jp.getString(path));
    }


    /**
     * US03-2
     */

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() throws SQLException {

        // API - EXPECTED - GET FROM REQUEST BODY
        LOG.info("EXPECTED BOOK DATA from API "+randomData);
        System.out.println("API = " + randomData);

        // DB  - ACTUAL - WRITE QUERY BY USING book_id
        String bookId = jp.getString("book_id");
        LOG.info("Book id is {} ",bookId);

        // how to create query in a different way
        String query= DatabaseHelper.getBookByIdQuery(bookId);
        DB_Util.runQuery(query);

        Map<String, Object> dbMap = DB_Util.getRowMap(1);
        dbMap.remove("id");
        dbMap.remove("added_date");
        LOG.info("ACTUAL BOOK DATA from DB "+dbMap);

        Assert.assertEquals(randomData,dbMap);


        // UI  - ACTUAL - OPEN UI GET CORRESPONDING FIELD DATA
        String bookName = (String)randomData.get("name");

        BookPage bookPage=new BookPage();
        bookPage.search.sendKeys(bookName);
        BrowserUtil.waitFor(3);

        bookPage.editBook(bookName).click();
        BrowserUtil.waitFor(3);

        Map<String, Object> uiMap = new LinkedHashMap<>();
        String uiBookName = bookPage.bookName.getAttribute("value");
        uiMap.put("name",uiBookName);

        String uiISBN = bookPage.isbn.getAttribute("value");
        uiMap.put("isbn",uiISBN);

        String uiYear=bookPage.year.getAttribute("value");
        uiMap.put("year",uiYear);

        String uiAuthor=bookPage.author.getAttribute("value");
        uiMap.put("author",uiAuthor);

        String uiDesc=bookPage.description.getAttribute("value");
        uiMap.put("description",uiDesc);

        // Get Book Category id
        // Get book category name from UI
        String selectedCategory = BrowserUtil.getSelectedOption(bookPage.categoryDropdown);
        System.out.println("selectedCategory = " + selectedCategory);

        String query2 = DatabaseHelper.getCategoryIdQuery(selectedCategory);
        DB_Util.runQuery(query2);


        String uiCategoryID = DB_Util.getFirstRowFirstColumn();
        uiMap.put("book_category_id",uiCategoryID);
        LOG.info("ACTUAL BOOK DATA from UI "+uiMap);

        Assert.assertEquals(randomData,uiMap);



    }
}
