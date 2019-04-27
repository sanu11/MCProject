package com.example.projectapp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataDeserializer {

    private static final String COMMA_DELIMITER = ",";

    private InputStream inputStream;

    public DataDeserializer(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public List deserializeHRSample() {
        List<float[]> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            float test[];
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(COMMA_DELIMITER);
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

    public ArrayList<Sample> deserializeSamples() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Sample> samples = new ArrayList<>();
        String line = "";
        reader.readLine();
        while ((line = reader.readLine()) != null)
        {
            String[] sampleDetails = line.split(COMMA_DELIMITER);
            if(sampleDetails.length > 0 )
            {
                Sample s = new Sample(Float.parseFloat(sampleDetails[0]), Float.parseFloat(sampleDetails[1]), sampleDetails[2]);
                samples.add(s);
            }
        }
        return samples;
    }
}