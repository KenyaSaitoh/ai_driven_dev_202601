package pro.kensait.berrybooks.cucumber;

import org.junit.platform.suite.api.*;

/**
 * Cucumber テストランナー
 * 
 * JUnit Platform上でCucumberテストを実行
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/unit/common")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = "cucumber.glue", value = "pro.kensait.berrybooks.cucumber.steps")
public class CucumberTestRunner {
}
