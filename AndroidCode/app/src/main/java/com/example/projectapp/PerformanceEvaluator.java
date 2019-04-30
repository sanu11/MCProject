package com.example.projectapp;

import java.util.List;

public class PerformanceEvaluator {
    public PerformanceEvaluator(){

    }
    public float calculateAccuracy(List<String> actual, List<String> predicted) {
        float accuracy = 0;
        for(int i=0; i < actual.size(); i++) {
            if (actual.get(i).equals(predicted.get(i))) {
                accuracy += 1;
            }
        }
        return accuracy *100/actual.size();
    }

    public float calculateFalsePositive(List<String> actual, List<String> predicted) {
        int falsePositive= 0;
        for(int i=0; i < actual.size(); i++) {
            if (actual.get(i).equals("Negative")){
                if(predicted.get(i).equals("Positive")){
                    falsePositive += 1;
                }
            }
        }
        return falsePositive;
    }

    public float calculateFalseNegative(List<String> actual, List<String> predicted) {
        int falseNegative= 0;
        for(int i=0; i < actual.size(); i++) {
            if (actual.get(i).equals("Positive")){
                if(predicted.get(i).equals("Negative")){
                    falseNegative += 1;
                }
            }
        }
        return falseNegative;
    }
}
