package main.java.webTimerTest;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import main.java.webTimerTest.WebPageTimersClass.CompareMethod;
import main.java.perfecto.*;
import org.testng.annotations.Optional;

import java.io.IOException;
import java.util.*;


public class webTimerTestClass {
    RemoteWebDriver driver;
    ReportiumClient reportiumClient;
    WebPageTimersClass pageTimers;
    List<DataRepo> _references = new ArrayList<DataRepo>();
    String[] pagesToTest = {"Amazon.com", "Quality_DevOPS"};

    @BeforeClass
    public void beforeClass(){
        System.out.println("===>>> Entering: NewTestClass.beforeSuite()" );
        // preload the reference data into the repo
        for (int i = 0; i<pagesToTest.length; i++) {
            DataRepo repo = new DataRepo(pagesToTest[i]);
            repo._baseReference = WebPageTimersClass.buildWebPageTimersClassfromCSV(repo);
            repo._baseReference.getResourceTimers().setTotals();
            _references.add(repo);
        }
        System.out.println("===>>> Exiting: NewTestClass.beforeSuite()" );

    }

    @Parameters({"platformName", "platformVersion", "browserName", "browserVersion", "screenResolution"})
    @BeforeTest
    public void beforeTest(String platformName, @Optional("Empty") String platformVersion, @Optional("Empty") String browserName, @Optional("Empty") String browserVersion, @Optional("Empty") String screenResolution) throws IOException {
        System.out.println("===>>> Entering: NewTestClass.beforeTest()" );
        System.out.println(System.getProperty("reportium-job-name") + Integer.parseInt(System.getProperty("reportium-job-number")));
        driver = Utils.getRemoteWebDriver(platformName, platformVersion, browserName, browserVersion, screenResolution);
        PerfectoExecutionContext perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
                .withProject(new Project("My Project", "1.0"))
                .withJob(new Job(System.getProperty("reportium-job-name"), Integer.parseInt(System.getProperty("reportium-job-number"))))
                .withContextTags("tag1")
                .withWebDriver(driver)
                .build();
        reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
        System.out.println("===>>> Exiting: NewTestClass.beforeTest()" );
    }

    @Test
    public void testAmazonHomePageLoad() throws Exception {
        try {
            System.out.println("===>>> Entering: NewTestClass.testAmazonHomePageLoad()" );
            reportiumClient.testStart("Perfecto Web Timers Test - Amazon Home Page", new TestContext("Performance", "Amazon"));
            System.out.println("Yay");

            driver.get("http://www.amazon.com");
                
            // obtain the data from the page
            pageTimers = new WebPageTimersClass(driver, "Amazon Home");
            
            // compare vs. previous page, set the comparison method, Min/Max/Avg and KPI(expected page load time in mili seconds)
            String analyzeResult = analyzeWebTimers("Amazon.com", 5000, WebPageTimersClass.CompareMethod.VS_BASE);
            
            if(analyzeResult != null)
            	throw new Exception(analyzeResult);

            reportiumClient.testStop(TestResultFactory.createSuccess());
        } catch (Exception e) {
        	if(e.toString().contains("took much longer to load")) {
	        	//Fail test with custom failure reason
	            reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e, "RkRs7c3EVT"));
        	} else {
        		reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
        	}
            //
            throw new Exception(e.toString());
        }
        System.out.println("===>>> Exiting: NewTestClass.test()" );
    }

    @Test
    public void testSearchAmazonQualityDevOpsBook() throws Exception {
        try {
            System.out.println("===>>> Entering: NewTestClass.testSearchAmazonQualityDevOpsBook()" );
            reportiumClient.testStart("Perfecto Web Timers Test - Amazon Search Qulity DevOps Book", new TestContext("Performance", "Quality DevOps Book"));
            System.out.println("Yay");

            driver.get("http://www.amazon.com");

            driver.findElementById("twotabsearchtextbox").sendKeys("The Digital Quality Handbook");
            //driver.findElementById("twotabsearchtextbox").submit();
            driver.findElementById("nav-search-submit-button").click();

            driver.findElement(By.xpath("//a[contains(@href, 'Digital-Quality')]")).click();
            driver.findElementById("add-to-cart-button");

            pageTimers = new WebPageTimersClass(driver, "Quality DevOPS Book");
            // compare vs. previous page, set the comparison method, Min/Max/Avg and KPI(expected page load time in mili seconds)
            String analyzeResult = analyzeWebTimers("Quality_DevOPS",  5000, WebPageTimersClass.CompareMethod.VS_AVG);

            if(analyzeResult != null)
            	throw new Exception(analyzeResult);
            reportiumClient.testStop(TestResultFactory.createSuccess());
        } catch (Exception e) {
        	if(e.toString().contains("took much longer to load")) {
	        	//Fail test with custom failure reason
	            reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e, "RkRs7c3EVT"));
        	} else {
        		reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
        	}
            //
            throw new Exception(e.toString());

        }
        System.out.println("===>>> Exiting: NewTestClass.test()" );
    }

    @AfterTest
    public void afterTest() {
        System.out.println("===>>> Entering: NewTestClass.afterTest()" );
        try {
            // Retrieve the URL of the Single Test Report, can be saved to your execution summary and used to download the report at a later point
            String reportURL = reportiumClient.getReportUrl();
            System.out.println("Report URL:" + reportURL);

            driver.close();
            System.out.println("Report: " + reportURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driver.quit();
        System.out.println("===>>> Exiting: NewTestClass.afterTest()" );
    }


    private String analyzeWebTimers(String pageName, int KPI, CompareMethod method) throws NoSuchFieldException, IllegalAccessException {
        System.out.println("===>>> Entering: NewTestClass.analyzeWebTimers()" );
        // find the reference object to compare against this page
        int i = Arrays.asList(pagesToTest).indexOf(pageName);
        String Errormessage = null;
        Boolean slowPageLoad = false;

        System.out.println(_references.get(i).minDuration);
        System.out.println(_references.get(i).maxDuration);
        System.out.println(_references.get(i).avgDuration);

        Map<String,String> ValueMap = new HashMap<String,String>();

        // compare vs. previous page, set the comparison method, Min/Max/Avg and KPI
        ValueMap = pageTimers.comparePagePerformance(KPI, method, _references.get(i)._baseReference, _references.get(i).minDuration, _references.get(i).maxDuration,
                _references.get(i).avgDuration);
        if (ValueMap.get("TestConditionResult").equalsIgnoreCase("true")) {
            slowPageLoad = true;
            System.out.println("Page "+ pageTimers.getRunName()+ " took much longer to load!!! ");
            Errormessage = "Failed! the Page "+ pageTimers.getRunName()+ " took much longer to load!!! \n"
                    + "Browser Name = " + ValueMap.get("BrowserName")+ "\n"
                    + "Platform Name = " + ValueMap.get("PlatformName")+ "\n"
            		+ "current page Duration = " + ValueMap.get("CurrentPageDuration")+ "\n"
            		+ "Base Reference Duration = " +  ValueMap.get("BaseReference") + "\n"
            		+ "Base Average Duration = " + ValueMap.get("BaseAvgDuration") + "\n"
                    + "Base Max Reference = " + ValueMap.get("BaseAvgDuration") + "\n"
                    + "Comparison Method = " + ValueMap.get("ComparisonMethod") + "\n"
                    + "KPI = " + KPI + "\n";
        }
        pageTimers.appendToCSV(pageName);
        // analyze and compare the page vs. reference and print to CSV
        pageTimers.conductFullAnalysisAndPrint(pageName, _references.get(i)._baseReference);
        if (null != pageTimers)
            System.out.println(pageTimers);
        System.out.println("===>>> Exiting: NewTestClass.analyzeWebTimers.5()" );
        return Errormessage;
    }

    // this is a data repo for each page in the test for comparison based on previously recorded data
    class DataRepo{
        WebPageTimersClass _baseReference;
        Double minDuration;
        Double maxDuration;
		double avgDuration;
        String pageName;
        public DataRepo(String pageName){
            super();
            this.pageName = pageName;
            minDuration=Double.MAX_VALUE;
            maxDuration = (double) 0L;
            avgDuration = 0L;
        }
    }

}