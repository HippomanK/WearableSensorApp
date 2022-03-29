package no.nordicsemi.android.blinky;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Graph extends AppCompatActivity {


    GraphView graphView;
    LineGraphSeries<DataPoint> data;
    DatabaseHelper db;
    Integer n_coughing;
    int ch1,ch2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        db = new DatabaseHelper(this);
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
        if(Prediction.equals("coughing")){
            n_coughing++;

           DateTimeFormatter date_f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
           DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH:mm:ss");

           //get the time and the date
           String Date = LocalDateTime.now().format(date_f).toString();
           String Time = LocalDateTime.now().format(time_f).toString();
           //Insert the data and the time it has been registered
           db.insertData(0,Date,"coughing",Time);
        }
    }
}