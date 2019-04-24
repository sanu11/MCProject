package com.example.projectapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.res.AssetManager;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity {

    private Classifier SVM = null;
    private PerformanceEvaluator evaluator = new PerformanceEvaluator();
    private String modelSelected = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] arraySpinner = new String[] {
                "Person 1", "Person 2", "Person 3", "Person 4", "Custom"
        };
        Spinner s = (Spinner) findViewById(R.id.spinner);
        final Button uploadFile = findViewById(R.id.uploadFileButton);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(selectedItem.equals("Custom"))   {
                    findViewById(R.id.uploadFileButton).setVisibility(View.VISIBLE);
                    GraphView graphView = findViewById(R.id.graphInterface);
                    graphView.removeAllSeries();
                    graphView.setVisibility(View.GONE);
                    //TODO : handle case of Custom selected and Detect clicked without uploading file
                }
                else
                    findViewById(R.id.uploadFileButton).setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button detectButton = findViewById(R.id.detectButton);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("detectButton", "DetectButtonClick Handler called");
                onDetectButtonClickHandler();
            }
        });

        Button predictButton = findViewById(R.id.predictButton);
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("predictButton", "predictButtonClick Handler called");
                try {
                    openDialog();
                    onPredictButtonClickHandler();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void openDialog()    {
        final Dialog modelMenuDialog = new Dialog(this);
        modelMenuDialog.setTitle("Select Model");
        modelMenuDialog.setContentView(R.layout.model_menu);
        List<String> optionList = new ArrayList<>();
        optionList.add("SVM");
        optionList.add("Logistic Regression");
        optionList.add("Decision Tree");
        optionList.add("Model 4");  //TODO: rename Model 4
        RadioGroup radioGroup = (RadioGroup) modelMenuDialog.findViewById(R.id.radio_group);

        for(int i=0;i<optionList.size();i++){
            RadioButton radioButton = new RadioButton(this); // dynamically creating RadioButton and adding to RadioGroup.
            radioButton.setText(optionList.get(i));
            radioButton.setTextSize(20.0f);
            radioButton.setTextAppearance(R.style.TextAppearance_AppCompat_Light_Widget_PopupMenu_Large);
            radioGroup.addView(radioButton);
        }

        modelMenuDialog.show();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int i) {
                int childCount = rg.getChildCount();
                for(int x=0; x<childCount; x++)   {
                    RadioButton rb = (RadioButton) rg.getChildAt(x);
                    if(rb.getId() == i) {
                        modelSelected = rb.getText().toString();
                        Log.d("radioGroupSelection","" + rb.getText().toString());
                    }
                }
                modelMenuDialog.dismiss();
            }
        });

    }

    public void plotGraph(float[] record){

        GraphView graphView = (GraphView) findViewById(R.id.graphInterface);
        int n =record.length;
        DataPoint[] dataPoints =new DataPoint[n];

        DataPoint temp=null;
        for(int i=1;i<=n;i++){
            temp= new DataPoint(i,record[i-1]);
            dataPoints[i-1] = temp;
        }


        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        series.setColor(Color.BLUE);
        graphView.addSeries(series);

        List<DataPoint> highlightedPoints =new ArrayList<DataPoint>();
        DataPoint hdp = null;
        for(int i=1;i<=n;i++){
            if(record[i-1] <= 60) {
                hdp = new DataPoint(i, record[i - 1]);
                highlightedPoints.add(hdp);
            }
        }
        DataPoint[] highlightedDP = new DataPoint[highlightedPoints.size()];
        highlightedDP = highlightedPoints.toArray(highlightedDP);
        PointsGraphSeries<DataPoint> pointsGraphSeries = new PointsGraphSeries<DataPoint>(highlightedDP);
        pointsGraphSeries.setColor(Color.RED);
        pointsGraphSeries.setSize(10.0f);
        graphView.addSeries(pointsGraphSeries);
    }

    public void falsePositive(float[] hr){

        int cnt_threshold=3,window=6;
        Double sum=0.0;
        int start=0;
        int end;
        int brady=0;
        float threshold = 60.0f;

        int[] manual_label = new int[hr.length];
        int[] detect_label = new int[hr.length];
        //manual annotation based on threshold
        for(int i=0;i<hr.length;i++)
        {
            if(hr[i]<=threshold)
                manual_label[i]=1;
            else
                manual_label[i]=0;

        }
        // Time-window based detection algorithm
        while(true)
        {
            end=start+(window-1);
            if(end==hr.length)
                break;
            brady=0;

            //getting count of braycardia in the threshold window

            for(int j = start;j<end;j++)
            {
                float curr_hr=hr[j];
                if(curr_hr<=threshold)
                {
                    brady++;
                }

            }
            //labelling all values within window based on threshold count
            if(brady >cnt_threshold)//set to 0 when above threshold
            {
                for(int i=start;i<start+(window-1);i++)
                {
                    //System.out.println("I value within greater than count threshold: "+i+" HR:"+hr.get(i));
                    detect_label[i]=1;
                    //System.out.println("Within greater not 1 already "+detect_label[i]);

                }

            }
            else//set to 1 when below or equal to count threshold
            {
                for(int i=start;i<start+(window-1);i++)
                {  // System.out.println("I value lesser than count threshold: "+i+" HR:"+hr.get(i));
                    if((detect_label[i]==1)) //&&((Double)(hr.get(i))<=threshold))

                    {
                        // System.out.println("Within greater already 1 "+detect_label[i]);
                        continue;
                    }
                    else
                    {
                        detect_label[i]=0;
                    }
                    //System.out.println("Within lesser than count threshold "+detect_label[i]);
                }
            }
            start++;
        }
        //computing FP and FN
        int fn=0,fp=0;
        for(int i=0;i<detect_label.length;i++)
        {
            if ((manual_label[i]==1) && (detect_label[i]==0))
            {
                fn++;
            }
            if(manual_label[i]==0 && detect_label[i]==1)
            {
                fp++;
            }

        }
        boolean isBradyCardia=false;
        String bradyCardiaResult=null;
        Log.d("manual_label", ""+ Arrays.toString(manual_label));
        Log.d("detect_label", ""+ Arrays.toString(detect_label));

        //Show if the concerned patient has bradycardia
        for (int p:manual_label) {
            if (p!=0){
                isBradyCardia = true;
                bradyCardiaResult = "Positive";
                break;
            }else {bradyCardiaResult = "Negative";}
        };

        Log.d("falsePositive","False Positive: "+fp+"False Negatives: "+fn);
        TextView falsePositive = findViewById(R.id.falsePositive);
        falsePositive.setText("False Positive: " + fp);
        TextView falseNegative = findViewById(R.id.falseNegtive);
        falseNegative.setText("False Negative: " + fn);
        //Toast.makeText(getApplicationContext(),"fp:"+fp+"fn"+fn,Toast.LENGTH_LONG).show();
    }


    public void onDetectButtonClickHandler() {
        GraphView graph = (GraphView) findViewById(R.id.graphInterface);
        graph.setVisibility(View.VISIBLE);
        graph.removeAllSeries();
        Spinner spinner = findViewById(R.id.spinner);
        String selectedItem = spinner.getSelectedItem().toString();
        Log.d("DetectButtonClick",""+selectedItem);
        Button uploadFile = findViewById(R.id.uploadFileButton);

        DataDeserializer dataDeserializer = null;
        InputStream inputStream = null;
        List<float[]> heartRateRecords = new ArrayList<float[]>();
        if(selectedItem.equals("Person 1")) {
            inputStream = getResources().openRawResource(R.raw.ekg_raw_16272_person1);
            dataDeserializer = new DataDeserializer(inputStream);
            heartRateRecords = dataDeserializer.deserializeHRSample();
            plotGraph(heartRateRecords.get(0));
            Log.d("HeartRate",""+ Arrays.toString(heartRateRecords.get(0)));
        }
        else if(selectedItem.equals("Person 2"))  {
            inputStream = getResources().openRawResource(R.raw.ekg_raw_16273_person2);
            dataDeserializer = new DataDeserializer(inputStream);
            heartRateRecords = dataDeserializer.deserializeHRSample();
            plotGraph(heartRateRecords.get(0));
            Log.d("HeartRate",""+ Arrays.toString(heartRateRecords.get(0)));

        }
        else if(selectedItem.equals("Person 3"))  {
            inputStream = getResources().openRawResource(R.raw.ekg_raw_16420_person3);
            dataDeserializer = new DataDeserializer(inputStream);
            heartRateRecords = dataDeserializer.deserializeHRSample();
            plotGraph(heartRateRecords.get(0));
            Log.d("HeartRate",""+ Arrays.toString(heartRateRecords.get(0)));

        }
        else if(selectedItem.equals("Person 4"))  {
            inputStream = getResources().openRawResource(R.raw.ekg_raw_16483_person4);
            dataDeserializer = new DataDeserializer(inputStream);
            heartRateRecords = dataDeserializer.deserializeHRSample();
            plotGraph(heartRateRecords.get(0));
            Log.d("HeartRate",""+ Arrays.toString(heartRateRecords.get(0)));
        }
        else if(selectedItem.equals("Custom"))   {
            Toast.makeText(MainActivity.this, "Select Upload file", Toast.LENGTH_SHORT).show();
            //TODO
        }
        falsePositive(heartRateRecords.get(0));
    }


    public List<String> predictBradycardia(Classifier model, ArrayList<Sample> testData) {

        ArrayList<String> predOutput = new ArrayList<>();

        final Attribute attributeVariance = new Attribute("Variance");
        final List<String> classes = new ArrayList<String>(){
            {
                add("Positive");
                add("Negative");
            }
        };

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>(1){
            {
                add(attributeVariance);
                Attribute attributeClass = new Attribute("@@class@@",classes);
                add(attributeClass);
            }
        };

        for (int i = 0; i < testData.size(); i++) {
            Instances dataUnpredicted = new Instances("TestInstances",
                    attributeList, 1);
            dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

            int currSample = i;
            DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
                {
                    setValue(attributeVariance, testData.get(currSample).getVariance());
                }
            };
            newInstance.setDataset(dataUnpredicted);

            try {
                double result = model.classifyInstance(newInstance);
                String className = classes.get(new Double(result).intValue());
                predOutput.add(className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return predOutput;
    }

    @SuppressLint("LongLogTag")
    public void onPredictButtonClickHandler() throws IOException {
        long startTime = System.nanoTime();

        DataDeserializer deserializer = new DataDeserializer(getResources().openRawResource(R.raw.data_test_272));
        ArrayList<Sample> samples = deserializer.deserializeSamples();
        Log.d("Size of sample", String.valueOf(samples.size()));

        AssetManager assetManager = getAssets();

        try{
            SVM = (Classifier) weka.core.SerializationHelper.read(assetManager.open("svm272.model"));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> actual = new ArrayList<>();
        for (int i=0; i<samples.size();i++){
            actual.add(samples.get(i).getLabel());
        }

        List<String> predictions  = predictBradycardia(SVM, samples);
        long endTime = System.nanoTime();

        double executionTime = (double) (endTime - startTime)/1000000;
        Log.d("execution time in miliseconds", String.valueOf(executionTime));
        float accuracy = evaluator.calculateAccuracy(actual, predictions);
        Log.d("accuracy", String.valueOf(accuracy));
        float falseNegative = evaluator.calculateFalseNegative(actual, predictions);
        Log.d("false negative", String.valueOf(falseNegative));
        float falsePositive = evaluator.calculateFalsePositive(actual, predictions);
        Log.d("false positive", String.valueOf(falsePositive));
    }
}
