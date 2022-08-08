package main.java.perfecto;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class WebTimersHandler {
    public static String printResourceTimers(RemoteWebDriver w)
    {
        List<Map<String, String>> resourceTimers = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, Object>> resourceTimersO =   (ArrayList<Map<String,Object>>) w.executeScript("var a =  window.performance.getEntriesByType(\"resource\") ;     return a; ", resourceTimers);
        System.out.println(resourceTimersO.getClass().toString());
        return printResourceTimers(resourceTimersO);

    }
    private static String printResourceTimers( ArrayList<Map<String, Object>> data)
    {
        String timers = " ** RESOURCE TIMERS **";
        for (int i=0; i< data.size(); i++) {
            String name = data.get(i).get("name").toString();
            String type = data.get(i).get("initiatorType").toString();
            double responseEnd = Double.parseDouble(data.get(i).get("responseEnd").toString());
            double startTime = Double.parseDouble(data.get(i).get("startTime").toString());
            long size = Long.parseLong(data.get(i).get("transferSize").toString());

            timers = timers + System.lineSeparator() + "Resource index := " + i +  System.lineSeparator() + " name := " + name;
            System.out.println("Resource " + i +  " name := " + name);
            timers = timers + System.lineSeparator() +" size := " + size;
            System.out.println(" size := " + size);
            timers = timers + System.lineSeparator() +" type := " + type;
            System.out.println(" type := " + type);
            timers = timers + System.lineSeparator() +" E2E time := " + (responseEnd - startTime);
            System.out.println(" E2E time := " + (responseEnd - startTime));
            timers = timers + System.lineSeparator() + " ** **** **";
            System.out.println(" ** **** **");
        }
        return timers;

    }

}
