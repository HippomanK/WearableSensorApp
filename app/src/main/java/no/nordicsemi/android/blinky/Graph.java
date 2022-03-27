package no.nordicsemi.android.blinky;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Graph extends AppCompatActivity {


    GraphView graphView;
    LineGraphSeries<DataPoint> data;
    int ch1,ch2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graphView = findViewById(R.id.graph);
        data = new LineGraphSeries<>(new DataPoint[]{});

        //Start python
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        Python py = Python.getInstance();
        //Access the ml module
        PyObject module = py.getModule("test");
        //get the predictions from the ml module
        PyObject Prediction = module.callAttr("evaluate",data);
        //debug the ml module
        System.out.println(Prediction);

    }
}