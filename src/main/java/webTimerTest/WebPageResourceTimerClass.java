package main.java.webTimerTest;

import org.openqa.selenium.remote.RemoteWebDriver;
import main.java.perfecto.CSVHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static main.java.webTimerTest.ResourceDetails.RESOURCE_DETAIL_CSV_FILE_HEADER;
import static main.java.webTimerTest.ResourceTypeStats.PAGE_TYPE_CSV_FILE_HEADER;
import static main.java.perfecto.CSVHandler.COMMA_DELIMITER;
import static main.java.perfecto.CSVHandler.NEW_LINE_SEPARATOR;

public class WebPageResourceTimerClass {
    // Meta data
    private long id;
    private String runName;
    private String date;
    private String time;
    private String page;
    private String OSName, OSVersion, browserName, browserVersion;
    // Page resource Array
    private List<ResourceDetails> resourceDetailsArray;
    // page resource stats array
    private List<ResourceTypeStats> resourceTypeStats;

    // computing totals
    private long totalResources, totalResourcesSize = 0;
    private double totalResourcesDuration = 0.0;

    // both for when comparing pages
    private List<ResourceDetails> pageResourceDiff;
    private List<ResourceTypeStats> pageTypeDiff;

    //Resource attributes index for CSV reading
    private enum ResourceTimers {
        ID,
        NAME,
        DATE,
        TIME,
        PAGE,
        OS_NAME,
        OS_VERSION,
        BROWSER_NAME,
        BROWSER_VERSION,
        RESOURCE_NAME,
        RESOURCE_TYPE,
        RESOURCE_SIZE,
        RESOURCE_DURATION;
    }


    private static final String CSV_FILE_HEADER = "id, name, date, time, page, OS name, OS version, browser name, browser version, name, type, size, duration";

    //  ************* Constructors

    // build a web page timers class from a web page driver
    WebPageResourceTimerClass(RemoteWebDriver w, String name) {
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
        this.resourceDetailsArray = new ArrayList<ResourceDetails>();
        this.resourceTypeStats = new ArrayList<ResourceTypeStats>();
        this.pageTypeDiff = new ArrayList<ResourceTypeStats>();
        this.pageResourceDiff = new ArrayList<ResourceDetails>();

        // fill in the resource and types data from the page
        List<Map<String, String>> resourceTimers = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, Object>> resourceTimersO =   (ArrayList<Map<String,Object>>) w.executeScript("var a =  window.performance.getEntriesByType(\"resource\") ;     return a; ", resourceTimers);
        System.out.println(resourceTimersO.getClass().toString());
        organizePageResourceTimers(resourceTimersO);

    }
    // fill in the details for the resources, totals and types
    private void organizePageResourceTimers( ArrayList<Map<String, Object>> data) {
        ResourceDetails rd;
        for (int i=0; i< data.size(); i++) {
            String name = data.get(i).get("name").toString();
            try {
                if (name.contains(","))
                    name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String type = data.get(i).get("initiatorType").toString();
            double responseEnd = Double.parseDouble(data.get(i).get("responseEnd").toString());
            double startTime = Double.parseDouble(data.get(i).get("startTime").toString());
            long size = 0;
            try {
                size = Long.parseLong(data.get(i).get("transferSize").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            rd = new ResourceDetails(name, type, size, responseEnd - startTime);
            resourceDetailsArray.add(rd);
            classifyResourceTypes(rd);
            System.out.println("Resource Timing Added: " +  rd.toString());
        }
        setTotals();
    }

    // classify the types of resources as we read them
    private void classifyResourceTypes(ResourceDetails rd){
        for (ResourceTypeStats stat:resourceTypeStats)
            if (stat.getType().toLowerCase().equals(rd.getType().toLowerCase())){
                stat.setItems(stat.getItems() + 1);
                stat.setSize(stat.getSize()+rd.getSize());
                stat.setDuration(stat.getDuration()+ rd.getDuration());
                return;
            }

        // if we're here, it's a new type
        ResourceTypeStats stat = new ResourceTypeStats(rd.getType(), rd.getSize(), rd.getDuration());
        resourceTypeStats.add(stat);
    }

    // build from a string, we will need this for the CSV comparison
    private WebPageResourceTimerClass(String line) {
        super();
        String[] tokens = line.split(COMMA_DELIMITER);
        this.id = Long.parseLong(tokens[ResourceTimers.ID.ordinal()]);
        this.runName = tokens[ResourceTimers.NAME.ordinal()];
        this.date = tokens[ResourceTimers.DATE.ordinal()];
        this.time = tokens[ResourceTimers.TIME.ordinal()];
        this.page = tokens[ResourceTimers.PAGE.ordinal()];

        this.OSName = tokens[ResourceTimers.OS_NAME.ordinal()];
        this.OSVersion = tokens[ResourceTimers.OS_VERSION.ordinal()];
        this.browserName = tokens[ResourceTimers.BROWSER_NAME.ordinal()];
        this.browserVersion = tokens[ResourceTimers.BROWSER_VERSION.ordinal()];

        this.resourceDetailsArray = new ArrayList<ResourceDetails>();
        this.resourceTypeStats = new ArrayList<ResourceTypeStats>();
        this.pageTypeDiff = new ArrayList<ResourceTypeStats>();
        this.pageResourceDiff = new ArrayList<ResourceDetails>();
    }

    // empty constructor
    WebPageResourceTimerClass() {
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

        this.resourceDetailsArray = new ArrayList<ResourceDetails>();
        this.resourceTypeStats = new ArrayList<ResourceTypeStats>();
        this.pageTypeDiff = new ArrayList<ResourceTypeStats>();
        this.pageResourceDiff = new ArrayList<ResourceDetails>();
    }

    // Assist in building the data provider from CSV for comparisons
    public static WebPageResourceTimerClass buildWebPageTimersClassfromCSV(final String fileNameAdd) {
        // first find the file, it should include fileNameAdd and "base"
        // for example "webResourceTimers_Amazon.com_base_1513959194269.csv
        //File f = new File(System.getenv().get("LOCAL_PATH"));
        File f = new File(System.getProperty("LOCAL_PATH"));
        File[] matchingFiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().contains("base") && name.endsWith(".csv") && name.toLowerCase().contains(fileNameAdd.toLowerCase());
            }
        });
        // found no base reference files, return empty/initialized class
        if (0 == matchingFiles.length) return new WebPageResourceTimerClass();

        String fileName = matchingFiles[0].getAbsolutePath();

        // read from the CSV file
        List<String> csvFileStrings = CSVHandler.readCsvFile(fileName);

        // Is the file empty?
        if (null == csvFileStrings || csvFileStrings.size() == 0) return new WebPageResourceTimerClass();

        WebPageResourceTimerClass webPageResourceTimerClassReference = new WebPageResourceTimerClass(csvFileStrings.get(0));
        // Let's read the strings
        for (String line:csvFileStrings){
            // read the resource into class
            ResourceDetails rd = new ResourceDetails(line);
            // add to the array
            webPageResourceTimerClassReference.resourceDetailsArray.add(rd);
            // count totals and classify types
            webPageResourceTimerClassReference.classifyResourceTypes(rd);
            System.out.println("Resource Timing Added: " +  rd.toString());

        }
        return webPageResourceTimerClassReference;
    }
    // set the totals for the structure
    public void setTotals(){

        for (ResourceDetails rd:this.resourceDetailsArray) {
            totalResourcesSize = totalResourcesSize+rd.getSize();
            totalResourcesDuration = totalResourcesDuration+rd.getDuration();
        }
        totalResources = resourceDetailsArray.size();
    }

    // ************* Print out data
    @Override
    public String toString(){
        String s = System.lineSeparator()+"***********   Resource Details:   ***********"+
                System.lineSeparator()+ "Page ID= "+id+
                System.lineSeparator()+ "runName= "+runName+
                System.lineSeparator()+ "executionTime= "+date+ " " + time+

                System.lineSeparator()+"Page= "+page+
                System.lineSeparator()+"OS Name= "+OSName+
                System.lineSeparator()+"OS Version= "+OSVersion+
                System.lineSeparator()+"Browser Name= "+browserName+
                System.lineSeparator()+"Browser Version= "+browserVersion+
                System.lineSeparator()+"Total Resources="+totalResources;
        // add the page resource stats
        s = s+ System.lineSeparator()+"***********   Resource Type Stats:   ***********"+ System.lineSeparator();
        for (ResourceTypeStats stat:resourceTypeStats)
            s = s+"***********" + System.lineSeparator()+stat.toString() +System.lineSeparator();
        s = s+ System.lineSeparator()+"***********";

        // add the page resource details
        s = s+ System.lineSeparator()+"***********   Resource Details Array:   ***********"+ System.lineSeparator();
        for(ResourceDetails rd:resourceDetailsArray)
            s = s+ "***********" + System.lineSeparator()+rd.toString() +System.lineSeparator();
        s = s+ System.lineSeparator()+"***********";
        return s;
    }
    // send to CSV. The file typically would look like: webResourceTimers_Amazon.com_1513959194269.csv
    public void appendToCSV(String fileNameAdd){
        // TODO define WEB_RESOURCE_TIMERS_FILE_NAME environment variable in order to set where the resource timers data will be saved. For example: webResourceTimers.csv

        String fileName = System.getProperty("LOCAL_PATH");
        if (null != System.getProperty("WEB_RESOURCE_TIMERS_FILE_NAME"))
            fileName = fileName+ System.getProperty("WEB_RESOURCE_TIMERS_FILE_NAME");
        else
            fileName = fileName + "webResourceTimers.csv";
        if (null != fileNameAdd)
            fileName = fileName.replace(".csv", "_"+fileNameAdd+"_.csv");
        // TODO define APPLY_TIMESTAMP_TO_RESOURCE_FILENAME environment variable in order to save the resource timers data in separate files every run. For example: true

        if (null != System.getProperty("APPLY_TIMESTAMP_TO_RESOURCE_FILENAME"))
            fileName = fileName.replace(".csv", System.currentTimeMillis()+".csv");
        // build the string and send to CSV
        CSVHandler.writeCsvFile(fileName, this.toCSVString(), CSV_FILE_HEADER);
    }
    // build the CSV string
    private String toCSVString(){
        String preamble = String.valueOf(id) + COMMA_DELIMITER;
        preamble = preamble + runName+ COMMA_DELIMITER;
        preamble = preamble + date+ COMMA_DELIMITER;
        preamble = preamble + time+ COMMA_DELIMITER;
        preamble = preamble + page+ COMMA_DELIMITER;

        preamble = preamble + OSName+ COMMA_DELIMITER;
        preamble = preamble + OSVersion+ COMMA_DELIMITER;
        preamble = preamble + browserName+ COMMA_DELIMITER;
        preamble = preamble + browserVersion+ COMMA_DELIMITER;

        // each line will include the preamble (metadata) + the resource data
        String details = "";
        for(int i = 0; i< resourceDetailsArray.size()-1; i++)
            details = details + preamble + resourceDetailsArray.get(i).toCSVString() + NEW_LINE_SEPARATOR ;
        details = details + preamble + resourceDetailsArray.get(resourceDetailsArray.size()-1).toCSVString();
        return details;
    }

    // Allow the page-level summary to include high level resource summary
    public String getPageResourceStatsSummaryForCSVString(){
        return totalResources + COMMA_DELIMITER + totalResourcesSize + COMMA_DELIMITER + totalResourcesDuration;
    }

    // ************* Comparison Operations


    // Full comparison of current page resources vs. whats been recorded in past runs
    public String conductFullAnalysisAndPrint(WebPageResourceTimerClass reference){
        // Compare the page level resource summary
        buildPageTypeStatsDiffs(reference.resourceTypeStats, pageTypeDiff);
        buildPageResourceDiffs(reference.resourceDetailsArray, pageResourceDiff);

        // format the strings
        String separator = NEW_LINE_SEPARATOR + "***********    title   **************" + NEW_LINE_SEPARATOR;

        // page type comparison
        String sendToCSV = separator.replace("title", "Page Type Diff Analysis") +
                PAGE_TYPE_CSV_FILE_HEADER + NEW_LINE_SEPARATOR;
        for (ResourceTypeStats stat:pageTypeDiff) {
            sendToCSV = sendToCSV + stat.toCSVStringWithDiff() + NEW_LINE_SEPARATOR;
        }

        // resource detail level comparison
        sendToCSV = sendToCSV + separator.replace("title", "Page Resource Diff Analysis") +
                RESOURCE_DETAIL_CSV_FILE_HEADER + NEW_LINE_SEPARATOR;
        for (ResourceDetails detail:pageResourceDiff) {
            sendToCSV = sendToCSV + detail.toCSVStringWithDiff()+ NEW_LINE_SEPARATOR;
        }

        System.out.println("Diff analysis:"+ System.lineSeparator()+pageTypeDiff.toString() + pageResourceDiff + toString());
        return sendToCSV;
    }

    // compare the type analysis arrays for this page and the reference
    private void buildPageTypeStatsDiffs( List<ResourceTypeStats> refStats, List<ResourceTypeStats> _local) {
        ResourceTypeStats diff = null;
        // go over the list and compare types to the reference
        for (ResourceTypeStats stat : this.resourceTypeStats) {
            boolean found = false;
            for (ResourceTypeStats refStat : refStats) {
                if (stat.getType().toLowerCase().equals(refStat.getType().toLowerCase())) {
                    // if found, add a new object with the comparison
                    diff = new ResourceTypeStats(stat.getType(), stat.getItems() - refStat.getItems(),
                            stat.getSize() - refStat.getSize(), stat.getDuration() - refStat.getDuration(),
                            "found match");
                    refStat.setComparisonResult("found match");
                    stat.setComparisonResult("found match") ;
                    found = true;
                    _local.add(diff);
                    break;
                }
            }
            if (!found) {
                // if not found, add the new object: these are types on the current page that do not exist on the reference
                diff = new ResourceTypeStats(stat.getType(), stat.getItems(), stat.getSize(), stat.getDuration(), "new");
                _local.add(diff);
            }
        }

// add the remaining types from the reference list
        for (ResourceTypeStats stat : refStats) {
            if (null != stat.getComparisonResult() && !stat.getComparisonResult().equals("found match")) {
                boolean found = false;
                for (ResourceTypeStats refStat : this.resourceTypeStats) {
                    if (stat.getType().toLowerCase().equals(refStat.getType().toLowerCase())) {
                        refStat.setComparisonResult("found match");
                        stat.setComparisonResult("found match");
                        found = true;
                        break;
                    }
                }
                // if it's not found then its a type that was on the reference but not on the new page
                if (!found) {
                    diff = new ResourceTypeStats(stat.getType(), (-1) * stat.getItems(),
                            (-1) * stat.getSize(), (-1) * stat.getDuration(), "exist only in reference");
                    stat.setComparisonResult("exist only in reference");
                    _local.add(diff);
                }
            }
        }
    }

        // compare the resources one by one
        private void buildPageResourceDiffs(List<ResourceDetails> refStatsArray, List<ResourceDetails> _localArray) {
            ResourceDetails diff = null;
            // go over the list and compare resources to the reference
            for (ResourceDetails rd:this.resourceDetailsArray){
                boolean found  = false;
                for(ResourceDetails refRD:refStatsArray) {
                    if (rd.getName().toLowerCase().equals(refRD.getName().toLowerCase())) {
                        // if found, add a new object with the comparison
                        diff = new ResourceDetails(rd.getName(), rd.getType(),
                                rd.getSize() - refRD.getSize(),
                                rd.getDuration() - refRD.getDuration(), "found match");
                        refRD.setComparisonResult("found match");
                        rd.setComparisonResult("found match");
                        found = true;
                        _localArray.add(diff);
                        break;
                    }
                }
                if (!found) {
                    // if not found, add the new object: these are types on the current page that do not exist on the reference
                    diff = new ResourceDetails(rd.getName(), rd.getType(), rd.getSize() , rd.getDuration() , "new");
                    _localArray.add(diff);
                }
            }

// add the remaining resources from the reference list
            for (ResourceDetails rd:refStatsArray) {
                if (null != rd.getComparisonResult() && !rd.getComparisonResult().equals("found match")) {
                    boolean found = false;
                    for (ResourceDetails refRD : this.resourceDetailsArray) {
                        if (rd.getName().toLowerCase().equals(refRD.getName().toLowerCase())) {
                            refRD.setComparisonResult("found match");
                            rd.setComparisonResult("found match");
                            found = true;
                            break;
                        }
                    }
                    // if it's not found then its a resource that was on the reference but not on the new page
                    if (!found) {
                        diff = new ResourceDetails(rd.getName(), rd.getType(),
                                (-1) * rd.getSize(), (-1) * rd.getDuration(), "exist only in reference");
                        _localArray.add(diff);
                    }
                }
            }

    }

}
