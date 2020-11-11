package main_app.metadata_storage;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVFieldChange {

    public static void main (String[] args){

        try {
            CSVFieldChange.removeParethesesFromCSV("/home/bruno/Desktop/pv_dataset/2011-2012_solar_home_electricity_data_datefix_25.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            CSVFieldChange.updateCSV("/home/bruno/Desktop/pv_dataset/2011-2012_solar_home_electricity_data_datefix_25.csv", 4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            CSVFieldChange.removeParethesesFromCSV("/home/bruno/Desktop/pv_dataset/2011-2012_solar_home_electricity_data_datefix_25.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update CSV by row and column
     *
     * @param fileToUpdate CSV file path to update e.g. D:\\chetan\\test.csv
     * @param col Column for which you need to update
     * @throws IOException
     */
    public static void updateCSV(String fileToUpdate, int col) throws IOException {

        File inputFile = new File(fileToUpdate);
        //SimpleDateFormat formatterOriginal = new SimpleDateFormat("dd-MMM-yy", Locale.US);
        SimpleDateFormat formatterOriginal = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat formatterFinal = new SimpleDateFormat("yyyy-MM-dd");

        // Read existing file
        List<String[]> csvBody = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(inputFile))) {
            csvBody = reader.readAll();
            // get CSV row column  and replace with by using row and column
            System.out.println("Updating...");
            for (int i = 1; i < csvBody.size(); i++){
                //csvBody.get(i)[col] = replace;
                String dateStr = csvBody.get(i)[col];
                Date date = formatterOriginal.parse(dateStr);
                String dateout = formatterFinal.format(date);
                csvBody.get(i)[col] = dateout;
                if (i % 5000 == 0)
                    System.out.println(i+"/"+csvBody.size()+" replaced");
            }
        } catch (CsvException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Writing...");
        // Write to CSV file which is open
        try (CSVWriter writer = new CSVWriter(new FileWriter(inputFile))) {
            writer.writeAll(csvBody);
            writer.flush();
        }
    }

    /**
     * Update CSV by row and column
     *
     * @param fileToUpdate CSV file path to update e.g. D:\\chetan\\test.csv
     * @param col Column for which you need to update
     * @throws IOException
     */
    public static void updateCSVMonthName(String fileToUpdate, int col) throws IOException {

        File inputFile = new File(fileToUpdate);
        SimpleDateFormat formatterOriginal = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatterFinal = new SimpleDateFormat("yyyy-MM-dd");

        // Read existing file
        List<String[]> csvBody = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(inputFile))) {
            csvBody = reader.readAll();
            // get CSV row column  and replace with by using row and column
            System.out.println("Updating...");
            for (int i = 1; i <= csvBody.size(); i++){
                //csvBody.get(i)[col] = replace;
                String dateStr = csvBody.get(i)[col];
                Date date = formatterOriginal.parse(dateStr);
                String dateout = formatterFinal.format(date);
                //csvBody.get(i)[col] = dateout;
                if (i % 5000 == 0)
                    System.out.println(i+"/"+csvBody.size()+" replaced");
            }
        } catch (CsvException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Writing...");
        // Write to CSV file which is open
        try (CSVWriter writer = new CSVWriter(new FileWriter(inputFile))) {
            writer.writeAll(csvBody);
            writer.flush();
        }
    }

    public static void removeParethesesFromCSV(String fileToUpdate) throws IOException {

        File inputFile = new File(fileToUpdate);
        // Read existing file
        List<String[]> csvBody = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(inputFile))) {
            csvBody = reader.readAll();
            // get CSV row column  and replace with by using row and column
            /*System.out.println("Updating...");
            for (int i = 1; i <= csvBody.size(); i++){
                //csvBody.get(i)[col] = replace;
                for (int j = 1; j <= csvBody.get(i).length; j++){
                    String row = csvBody.get(i)[j].replace("\"", "");
                    csvBody.get(i)[j] = row;
                }
                //csvBody.get(i)[col] = dateout;
                if (i % 5000 == 0)
                    System.out.println(i+"/"+csvBody.size()+" replaced");
            }*/
        } catch (CsvException e) {
            e.printStackTrace();
        }

        System.out.println("Writing...");
        // Write to CSV file which is open
        try (CSVWriter writer = new CSVWriter(new FileWriter(inputFile), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.RFC4180_LINE_END)) {
            writer.writeAll(csvBody);
            writer.flush();
        }
    }
}