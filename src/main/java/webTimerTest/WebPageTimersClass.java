package main.java.webTimerTest;

import org.openqa.selenium.remote.RemoteWebDriver;
import main.java.perfecto.CSVHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.perfecto.CSVHandler.COMMA_DELIMITER;
import static main.java.perfecto.CSVHandler.NEW_LINE_SEPARATOR;

public class WebPageTimersClass {
    private static final String CSV_FILE_HEADER = "id, name, date, time, page, OS name, OS version, browser name, browser version, duration, network, http Request, http response, build DOM, render, total resources, total resources size, total resources duration";
    // a bit of meta data
    private long id;
    private String runName;
    private String date;
    private String time;
    private String page;
    private String OSName, OSVersion, browserName, browserVersion;
    // page level timers
    private Double duration,
    networkTime,
    httpRequest,
    httpResponse,
    buildDOM,
    render;
    // resource timers class
    private WebPageResourceTimerClass resourceTimers;

    public WebPageResourceTimerClass getResourceTimers(){
        return resourceTimers;
    }

    //  ************* Constructors

    // build a web page timers class from a web page driver
    WebPageTimersClass(RemoteWebDriver w, String name) {
        super();
        this.page = w.getCurrentUrl();
        this.runName = name;
        this.date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        this.time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        this.OSName = w.getCapabilities().getCapability("platformName").toString();
        this.OSVersion = w.getCapabilities().getCapability("platformVersion").toString();
        this.browserName = w.getCapabilities().getCapability("browserName").toString();
        if(!(this.OSName.equalsIgnoreCase("Android") || this.OSName.equalsIgnoreCase("iOS")))
            this.browserVersion = w.getCapabilities().getCapability("browserVersion").toString();
        else
            this.browserVersion = "Empty";


//        // build the page timers from the driver
//        Map<String,String> pageTimers = new HashMap<String,String>();
//        Object pageTimersO =  w.executeScript("var a =  window.performance.timing ;     return a; ", pageTimers);
//        organizePageTimers_OldTimer ((Map<String, Double>) pageTimersO);
//        
        // build the page timers from the driver
        Map<String,String> pageTimers1 = new HashMap<String,String>();
        Object pageTimers2 =  w.executeScript("var a =  performance.getEntriesByType(\"navigation\")[0] ;     return a; ", pageTimers1);
        organizePageTimers ((Map<String, Double>) pageTimers2);
        
        resourceTimers = new WebPageResourceTimerClass(w, name);
    }

    // build from a string, we will need this for the CSV comparison
    private WebPageTimersClass(String line, String fileNameAdd){
        super();
        String[] tokens = line.split(COMMA_DELIMITER);
        this.id = Long.parseLong(tokens[PageTimers.ID.ordinal()]);
        this.runName = tokens[PageTimers.NAME.ordinal()];
        this.date = tokens[PageTimers.DATE.ordinal()];
        this.time = tokens[PageTimers.TIME.ordinal()];
        this.page = tokens[PageTimers.PAGE.ordinal()];

        this.OSName = tokens[PageTimers.OS_NAME.ordinal()];
        this.OSVersion = tokens[PageTimers.OS_VERSION.ordinal()];
        this.browserName = tokens[PageTimers.BROWSER_NAME.ordinal()];
        this.browserVersion = tokens[PageTimers.BROWSER_VERSION.ordinal()];
        this.duration = Double.parseDouble(tokens[PageTimers.DURATION.ordinal()]);
        this.networkTime = Double.parseDouble(tokens[PageTimers.NETWORK.ordinal()]);
        this.httpRequest = Double.parseDouble(tokens[PageTimers.HTTPREQ.ordinal()]);
        this.httpResponse = Double.parseDouble(tokens[PageTimers.HTTPRES.ordinal()]);
        this.buildDOM = Double.parseDouble(tokens[PageTimers.BUILDDOM.ordinal()]);
        this.render = Double.parseDouble(tokens[PageTimers.RENDER.ordinal()]);
        // build the rest of resource data from CSV
        this.resourceTimers = WebPageResourceTimerClass.buildWebPageTimersClassfromCSV(fileNameAdd);
    }

    // build from scratch.
    private WebPageTimersClass(){
        super();
        this.id = 0L;
        this.runName = "base";
        this.date = "1/1/70";
        this.time = "10:0:0";
        this.page = "base";
        this.OSName = "base";
        this.OSVersion = "base";
        this.browserName = "base";
        this.browserVersion = "base";
        this.duration = (double) 0L;
        this.networkTime = (double) 0L;
        this.httpRequest = (double) 0L;
        this.httpResponse = (double) 0L;
        this.buildDOM = (double) 0L;
        this.render = (double) 0L;
        this.resourceTimers = new WebPageResourceTimerClass();
    }

    //  ************* In order to build a global data provider to compare against
    public static WebPageTimersClass buildWebPageTimersClassfromCSV(webTimerTestClass.DataRepo repo) {
        // TODO define WEB_TIMERS_FILE_NAME environment variable in order to set where the project will attempt to read previously recorded page timers. For example: webTimers.csv
       // String fileName = System.getenv().get("LOCAL_PATH");
    	String fileName = System.getProperty("LOCAL_PATH");
        if (null != System.getProperty("WEB_TIMERS_FILE_NAME"))
            fileName = fileName+ System.getProperty("WEB_TIMERS_FILE_NAME");
        else
            fileName = fileName + "webTimers.csv";
        // which page to compare against
        if (null != repo.pageName)
            fileName = fileName.replace(".csv", "_"+repo.pageName+"_.csv");

        // read from the CSV file
        List<String> csvFileStrings = CSVHandler.readCsvFile(fileName);
        // Is the file empty? return empty object
        if (null == csvFileStrings || csvFileStrings.size() == 0) return new WebPageTimersClass();
        WebPageTimersClass baseReferenceToReturn = null;
        // Let's read the strings
        for (String line:csvFileStrings){
            // create page timers class, as well as the internal resource analysis
            WebPageTimersClass baseReference = new WebPageTimersClass(line, repo.pageName);
            System.out.println(repo.minDuration);
            System.out.println(baseReference.duration);
            System.out.println(repo.maxDuration);
            System.out.println(repo.avgDuration);
           
            if (repo.minDuration > baseReference.duration) repo.minDuration = baseReference.duration;
            if (repo.maxDuration < baseReference.duration) repo.maxDuration = baseReference.duration;
            repo.avgDuration = repo.avgDuration + baseReference.duration;
            // has a 'base' reference been defined? this will be the string 'base' in the name column
            if (csvFileStrings.size() == 1 || baseReference.runName.toLowerCase().equals("base") ){
                baseReferenceToReturn =  baseReference;
            }
        }
        repo.avgDuration = repo.avgDuration/csvFileStrings.size();
        if (null == baseReferenceToReturn)
            // there are lines in the CSV but we haven't found 'base'
            return new WebPageTimersClass(csvFileStrings.get(0), repo.pageName);
        // this should never happen
        else return baseReferenceToReturn;
    }

    
    private void organizePageTimers_OldTimer( Map<String, Double> data)
    {
        // see https://developer.mozilla.org/en-US/docs/Web/API/Resource_Timing_API/Using_the_Resource_Timing_API
        double navStart = data.get("navigationStart");
        double loadEventEnd = data.get("loadEventEnd");
        double connectEnd = data.get("connectEnd");
        double requestStart = data.get("requestStart");
        double responseStart = data.get("responseStart");
        double responseEnd = data.get("responseEnd");
        double domLoaded = data.get("domContentLoadedEventStart");

        this.duration = loadEventEnd - navStart;
        this.networkTime = connectEnd - navStart;
        this.httpRequest = responseStart - requestStart;
        this.httpResponse = responseEnd - responseStart;
        this.buildDOM = domLoaded - responseEnd;
        this.render = loadEventEnd - domLoaded;
        
        
        

        // in Edge, sometimes loadEventEnd is reported 0. Fun!
        if (0 >= this.duration) {
            this.duration = networkTime + httpRequest + httpResponse + buildDOM;
            this.render = (double) 0;
        }
        System.out.println("Page Timing: " +  this.toString());
    }
    
    private void organizePageTimers( Map<String, Double> data)
    {
        // see https://developer.mozilla.org/en-US/docs/Web/API/Resource_Timing_API/Using_the_Resource_Timing_API

        Double loadEventEnd = convertDouble(data.get("loadEventEnd"));
        Double connectEnd = convertDouble(data.get("connectEnd"));
        Double requestStart = convertDouble(data.get("requestStart"));
        Double responseStart = convertDouble(data.get("responseStart"));
        Double responseEnd = convertDouble(data.get("responseEnd"));
        Double domLoaded = convertDouble(data.get("domContentLoadedEventStart"));

        this.duration =  convertDouble(data.get("duration"));
        this.networkTime = connectEnd;
        this.httpRequest = responseStart - requestStart;
        this.httpResponse = responseEnd - responseStart;
        this.buildDOM = domLoaded - responseEnd;
        this.render = loadEventEnd - domLoaded;

        // in Edge, sometimes loadEventEnd is reported 0. Fun!
        if (0 >= this.duration) {
            this.duration = networkTime + httpRequest + httpResponse + buildDOM;
            this.render = (double) 0;
        }
        System.out.println("Page Timing: " +  this.toString());
    }


    public static double convertDouble(Object longValue){
        double valueTwo = -1; // whatever to state invalid!

        if( longValue instanceof Long)
            valueTwo = ((Long) longValue).doubleValue();
        else
            valueTwo = (Double) longValue;
        System.out.println(valueTwo);
        return valueTwo;
    }
    
    public String getRunName(){
        return this.runName;
    }

    @Override
    public String toString(){
        String send= System.lineSeparator()+"***********"+
                System.lineSeparator()+"Page ID= "+id+
                System.lineSeparator()+"runName= "+runName+
                System.lineSeparator()+"executionTime= "+date+ " " + time+

                System.lineSeparator()+"Page= "+page+
                System.lineSeparator()+"OS Name= "+OSName+
                System.lineSeparator()+"OS Version= "+OSVersion+
                System.lineSeparator()+"Browser Name= "+browserName+
                System.lineSeparator()+"Browser Version= "+browserVersion+

                System.lineSeparator()+"Duration= "+duration+
                System.lineSeparator()+"networkTime= "+networkTime+
                System.lineSeparator()+"httpRequest= "+httpRequest+
                System.lineSeparator()+"httpResponse= "+httpResponse+
                System.lineSeparator()+"buildDOM= "+buildDOM+
                System.lineSeparator()+"render= "+render+
                System.lineSeparator()+"***********" ;
        // Add the resource level summary
        if (null != this.resourceTimers)
            send = send +
                    resourceTimers.toString()+
                    System.lineSeparator()+"***********";
        return send;
    }

    // save to CSV. File name would look something like: webtimers_amazon.com_.csc
    public void appendToCSV(String fileNameAdd){
        String fileName = System.getProperty("LOCAL_PATH");
        // TODO define WEB_TIMERS_FILE_NAME environment variable in order to name your page timers file. For example: webTimers.csv
        if (null != System.getProperty("WEB_TIMERS_FILE_NAME"))
            fileName = fileName+ System.getProperty("WEB_TIMERS_FILE_NAME");
        else
            fileName = fileName + "webTimers.csv";
        if (null != fileNameAdd)
            fileName = fileName.replace(".csv", "_"+fileNameAdd+"_.csv");
        // create the data from the page and summary of the resource data to the CSV
        CSVHandler.writeCsvFile(fileName, this.toCSVString(), CSV_FILE_HEADER);

        // Add the resource level detail to a separate file
        if (null != this.resourceTimers)
            resourceTimers.appendToCSV(fileNameAdd);
    }

    private String toCSVString(){
        String send = String.valueOf(id) + COMMA_DELIMITER;
        // Add test meta data
        send = send + runName+ COMMA_DELIMITER;
        send = send + date+ COMMA_DELIMITER;
        send = send + time+ COMMA_DELIMITER;
        send = send + page+ COMMA_DELIMITER;
        send = send + OSName+ COMMA_DELIMITER;
        send = send + OSVersion+ COMMA_DELIMITER;
        send = send + browserName+ COMMA_DELIMITER;
        send = send + browserVersion+ COMMA_DELIMITER;
        // Add page timing details
        send = send + duration+ COMMA_DELIMITER;
        send = send + networkTime+ COMMA_DELIMITER;
        send = send + httpRequest+ COMMA_DELIMITER;
        send = send + httpResponse+ COMMA_DELIMITER;
        send = send + buildDOM+ COMMA_DELIMITER;
        send = send + render + COMMA_DELIMITER;
        // Add the resource level summary: total resources, size and duration
        if (null != this.resourceTimers)
            send = send+this.resourceTimers.getPageResourceStatsSummaryForCSVString();
        return send;
    }



    // compare current page load time vs. whats been recorded in past runs
    public Map<String, String> comparePagePerformance(int KPI, CompareMethod method, WebPageTimersClass reference, Double min, Double max, Double avg){
        Map<String,String> returnMap = new HashMap<String,String>();
        returnMap.put("CurrentPageDuration", duration.toString());
        returnMap.put("BaseReference", reference.duration.toString());
        returnMap.put("BaseAvgDuration", avg.toString());
        returnMap.put("BaseMaxDuration", max.toString());
        returnMap.put("BaseMinDuration", min.toString());
        returnMap.put("BrowserName", browserName.toString());
        returnMap.put("PlatformName", OSName.toString());




        switch(method){
            case VS_BASE:
                System.out.println("comparing current: "+duration +" against base reference: "+ reference.duration);
                //return (duration - reference.duration) > KPI;
                returnMap.put("TestConditionResult", String.valueOf(((duration > reference.duration) ||  (duration > KPI))));
                returnMap.put("ComparisonMethod", "comparing current: "+duration +" against base reference: "+ reference.duration);

                //return ((duration > reference.duration) ||  (duration > KPI));
                return returnMap;
            case VS_AVG:
                System.out.println("comparing current: "+duration +" against AVG: "+ avg);
                //return (duration - avg) > KPI;
                returnMap.put("TestConditionResult", String.valueOf(((duration > avg) ||  (duration > KPI))));
                returnMap.put("ComparisonMethod", "comparing current: "+duration +" against AVG: "+ avg);

                // return ((duration > avg) ||  (duration > KPI));
                return returnMap;
            case VS_MAX:
                System.out.println("comparing current: "+duration +"  against Max: "+ max);
                //return (duration - max) > KPI;
                returnMap.put("TestConditionResult", String.valueOf((duration - max) > KPI));
                returnMap.put("ComparisonMethod", "comparing current: "+duration +"  against Max: "+ max);

                return returnMap;
            case VS_MIN:
                System.out.println("comparing current: \"+duration +\"  against min: "+ min);
                //return (duration - min) > KPI;
                returnMap.put("TestConditionResult", String.valueOf((duration - min) > KPI));
                returnMap.put("ComparisonMethod", "comparing current: "+duration +"  against Min: "+ min);

                return returnMap;
            default:
                System.out.println("comparing current: \"+duration +\"  against AVG method was not defined N/A: "+ avg);
                //return false;
                returnMap.put("TestConditionResult", "false");
                return returnMap;
        }

    }

    // print page diff to CSV; typical page result would look like pageComparison_Amazon.com_<OS and browser>_<timestamp>.csv
    public void conductFullAnalysisAndPrint(String fileNameAdd, WebPageTimersClass baseReference){
        // TODO define WEB_PAGE_COMPARISON_FILE_NAME environment variable in order to name your comparison summary file. For example: pageComparison.csv
        String fileName = System.getProperty("LOCAL_PATH");
        if (null != System.getProperty("WEB_PAGE_COMPARISON_FILE_NAME"))
            fileName = fileName + "PageComparison_" + System.getProperty("WEB_PAGE_COMPARISON_FILE_NAME");
        else
            fileName = fileName + "PageComparison.csv";
        if (null != fileNameAdd)
            fileName = fileName.replace(".csv", "_"+fileNameAdd+"_"+this.OSName+"_"+this.OSVersion+"_"+this.browserName+"_"+this.browserVersion+"_"+System.currentTimeMillis()+"_.csv");

        // print the high level current vs. base page
        String separator = NEW_LINE_SEPARATOR + "***********    title   **************" + NEW_LINE_SEPARATOR;
        String sendToCSV = separator.replace("title", "Page Summary")+"run, "+ CSV_FILE_HEADER + NEW_LINE_SEPARATOR+
                "Current Run,"+this.toCSVString() + NEW_LINE_SEPARATOR+
                "Base Run,"+baseReference.toCSVString() + NEW_LINE_SEPARATOR;

        // handle page type stats: type analysis + detailed resources
        sendToCSV = sendToCSV + this.resourceTimers.conductFullAnalysisAndPrint(baseReference.getResourceTimers());

        CSVHandler.writeCsvFile(fileName, sendToCSV, "");
    }


    //Page attributes index for CSV reading
    private enum PageTimers {
        ID,
        NAME,
        DATE,
        TIME,
        PAGE,
        OS_NAME,
        OS_VERSION,
        BROWSER_NAME,
        BROWSER_VERSION,
        DURATION,
        NETWORK,
        HTTPREQ,
        HTTPRES,
        BUILDDOM,
        RENDER
    }

    // What method to compare page load time by
    public enum CompareMethod {
        VS_AVG,
        VS_MIN,
        VS_MAX,
        VS_BASE
    }


}
