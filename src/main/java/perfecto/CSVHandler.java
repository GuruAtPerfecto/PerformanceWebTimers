package main.java.perfecto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
// Thanks Ashraf Sarhan for this code
// https://examples.javacodegeeks.com/core-java/apache/commons/csv-commons/writeread-csv-files-with-apache-commons-csv-example/

public abstract class CSVHandler {
    public static final String COMMA_DELIMITER = ",";
    public static final String NEW_LINE_SEPARATOR = "\n";


    public static void writeCsvFile(String fileName, String appendToFile, String fileHeader) {

        FileWriter fileWriter = null;
        Boolean fileExist;

        try {
            File f = new File(fileName);
            fileExist = f.exists();
            fileWriter = new FileWriter(fileName, true);

            //Write the CSV file header
            if (!fileExist) {

                fileWriter.append(fileHeader);

                //Add a new line separator after the header
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            fileWriter.append(appendToFile);
            fileWriter.append(NEW_LINE_SEPARATOR);

            System.out.println("CSV file "+fileName+" was created successfully !!!");
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }

        }
    }


    public static List readCsvFile(String fileName) {
        BufferedReader fileReader = null;
        List times = new ArrayList();
        File f = new File(fileName);
        if (!f.exists()) return null;
        try {
            String line = "";

            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileName));

            //Read the CSV file header to skip it
            fileReader.readLine();
            //Read the file line by line starting from the second line
            while ((line = fileReader.readLine()) != null)
                times.add(line);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }
        return times;
    }




}
