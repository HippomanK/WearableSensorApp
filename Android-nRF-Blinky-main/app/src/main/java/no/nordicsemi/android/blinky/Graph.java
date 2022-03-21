package no.nordicsemi.android.blinky;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.LinearGradient;
import android.os.Bundle;

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
    }
}