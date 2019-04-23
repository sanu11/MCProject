package com.example.projectapp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadData {
    InputStream inputStream;
    double[] weights;
    double bias;
    public LoadData(InputStream inputStream){
        this.inputStream = inputStream;
    }
    public List read() {
        List<float[]> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            float test[];
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                test = new float[row.length];
                for (int c = 0; c < row.length; c++) {
                    test[c] = Float.parseFloat(row[c]);
                }
                resultList.add(test);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        return resultList;
    }
}