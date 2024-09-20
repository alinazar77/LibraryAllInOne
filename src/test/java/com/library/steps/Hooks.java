package com.library.steps;




import com.library.utility.ConfigurationReader;
import com.library.utility.DB_Util;
import com.library.utility.Driver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Hooks {
    public Logger LOG= LogManager.getLogger();

    @Before("@ui")
    public void setUp(){

        Driver.getDriver().manage().window().maximize();
        Driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        Driver.getDriver().get(ConfigurationReader.getProperty("library_url"));
        LOG.info(ConfigurationReader.getProperty("library_url")+" is OPENED");

    }

    @After("@ui")
    public void tearDown(Scenario scenario){
        if(scenario.isFailed()){
            final byte[] screenshot = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot,"image/png","screenshot");
        }

        Driver.closeDriver();
        LOG.info(ConfigurationReader.getProperty("library_url")+" is CLOSED");

    }

    @Before("@db")
    public void setUpDB(){
        LOG.info("Connecting to database...");
        DB_Util.createConnection();
    }

    @After("@db")
    public void tearDownDB(){
        LOG.info("close database connection...");
        DB_Util.destroy();
    }

    @Before()
    public void setBaseURI(){
        RestAssured.baseURI=ConfigurationReader.getProperty("library.baseUri");
        LOG.info("Base URI is "+RestAssured.baseURI);
    }

    @After()
    public void endScenario(Scenario scenario){
        // LOG.info("Test Result {} : {} ",scenario.getName(),scenario.getStatus());
        LOG.info("Test Result for "+scenario.getName()+" "+scenario.getStatus());
    }

}
