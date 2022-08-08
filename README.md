# WebTimers
This project demonstrates the usage of W3C Navigation Timing API (on any browser) to examine web page responsiveness and root cause in any test as part of the CI, including smoke, regression etc. The project is build such that it should be very easy to add the page performance analysis to any existing test.
Initially you can use this project to collect and analyze page and resource level timing for pages of your script. But advanced usage also allows you to compare the current run against previous executions by page load time, analysis of resources by total, types, size and duration, and then individual level resource analysis.

## Objective
Assume you have a desktop or mobile website, and you develop it as part of your agile cycle. Would it be useful to know, by page, by browser/version/OS/OS version whether suddenly you increased the page load time significantly?
Would it be good to know that immediately, say, as part of the smoke test on commit?
Also, if the time did increase, would it be helpful to know what resources this page download, the URL for each, size, time etc.?
And last, would it be helpful to have all the data stored in CSV, or exportable, such that you can show trends, identify degredations etc.?


Well, W3C Navigation Timing API (https://www.w3.org/TR/navigation-timing/#sec-navigation-info-interface) and the Resource Timing API (https://developer.mozilla.org/en-US/docs/Web/API/Resource_Timing_API/Using_the_Resource_Timing_API) allow you to do just that. They contain the necessary data at the page level as well as the resource level.</br>
All that is left for you is to collect and analyze these as a part of your script, compare to past data if you like, and save for future use.


*************
## Getting started
**To get started**, you need to

Define a few environment variables:

- **LOCAL_PATH**: the folder to save all files, for example: /Users/Amir-Perfecto/Downloads/WebTimers/
- **WEB_TIMERS_FILE_NAME**: Optional, the name of the page level web timers file, for example: webtimers.csv
- **WEB_RESOURCE_TIMERS_FILE_NAME**: Optional, the name of the resource timers data file, for example: webResourcetimers.csv
- **APPLY_TIMESTAMP_TO_RESOURCE_FILENAME**: Optional, set this to any value (ex.: true) to save the resource timers data in a separate file each run.

- **PERFECTO_CLOUD**: your cloud name, for example, abc.perfectomobile.com
- **PERFECTO_CLOUD_USERNAME**: your cloud username, for example, abd@perfectomobile.com
- **PERFECTO_CLOUD_SECURITY_TOKEN**: your cloud security token. You can get it in your cloud, following these instructions: http://developers.perfectomobile.com/display/PD/Security+Token

You will see that **3 files are created for every page**. Those 3 files are:
- **webTimers**_Amazon.com.csv: this file includes the page level timing for the page you're on (in this case, Amazon.com)
- **pageResourceTimers**_Amazon.com_<timestamp>.csv: this file includes details of all the resources downloaded to this page
- **pageComparison**_Amazon.com_<OS, OS Version, browser, browser version>_<timestamp>.csv: this file includes a comparison of the current execution vs. a comparison of previous execution.

To **set a comparison against a previous execution** simply
- add the word 'base' to one of the previously created page resource timers csv file
- run the project again

**Make changes and create your own script**

Head to NewTestClass.java
- Change **pagesToTest** String array to include the name of the pages you want to test
- Inside the test() method, create your own test script. For each page, call
```
                pageTimers = new WebPageTimersClass(driver, "name of the page");
                analyzeWebTimers("name to embed into the log file names");
```
For example:
```
                driver.get("http://www.amazon.com");
                pageTimers = new WebPageTimersClass(driver, "Amazon.com");
                analyzeWebTimers("Amazon.com");
```

*************
## Under the hood

This project contains the following classes and the following is their usage:

### NewTestClass
This is the test itself. It initiate the driver. Add the relevant objects:
```
WebPageTimersClass pageTimers; // this is for the page timing data in the script
List<DataRepo> _references; // this is basically a set of page data that is read from the file system before any test is run to compare against current execution.
String[] pagesToTest = {"page1", "page2"}; //set this array to preload the previous page data for comparison for each page in the test
```
and then, after each page you want to measure, you can:
                
```
                driver.get("http://www.amazon.com");
                // obtain the data from the page and analyze
                pageTimers = new WebPageTimersClass(driver, "Amazon Home");
                analyzeWebTimers("Amazon.com");
```

inside analyzeWebTimer(name):
- find the reference in the dataRepo array
- compare against it (select method: min, max, avg or 'base')
- print all the data to CSV
- compare against previous page, print result to CSV

*************
### WebPageTimerClass
This is where the page level timers are actually stored. In addition, there's a sub class called WebResourceTimerClass, with all the detailed resources and analysus.
The class has some metadata, such as OS/browser details, time of test execution etc., and then the detailed measured timers.

At a high level, the class does the following:
- Constructors (based on the webDriver or based on a String, more below)
- toString() overload
- A method to write to CSV (appendToCSV, toCSVString)
- A method to create read from CSV and fill in the class
- A method to compare current page load time vs. a reference (isPageLoadLongerThanBaseline). More on this below.

*************
## WebPageResourceTimerClass
This class contains the resource level details of the page. Some pages can contain hundreds of resources, it is possible to get a lot of data about them to find the reason for poor responsiveness and optimize the page for different platforms.
The class contains similar header information, such as OS, browser, execution time etc.
It also contains an array of a sub class called **ResourceDetails**. This class contains, for each resource, the name, type, size and load time for that resource. For example:

```
Name= https://www.xyz.com/image.jpg
Type= jpg
Size= 1148
Duration= 87.99999999999994
```

It also has an array of sub class called **ResourceTypeStats**, which summarizes the total stats per each resource type (items, size, duration).

Methods are similar to WebPageTimersClass

Lastly, there are 3 classes worth mentioning:
- ResourceDetails: these are the details of all individual resources
- ResourceTypeStats: stats about the types of resources (images, links,..)
- CSVHandler: handles the details of reading and writing to CSV (Kudos to Ashraf Sarhan: https://examples.javacodegeeks.com/core-java/apache/commons/csv-commons/writeread-csv-files-with-apache-commons-csv-example/)