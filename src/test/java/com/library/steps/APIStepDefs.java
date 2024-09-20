package com.library.steps;


import com.library.utility.ConfigurationReader;
import com.library.utility.LibraryAPI_Util;
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

import java.util.HashMap;
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

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String dataType) {
        Map<String, Object> randomData=new HashMap<>();

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

}
