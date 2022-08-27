package no.nordicsemi.android.blinky;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.blinky.utils.FilterUtils;

public class Graph extends AppCompatActivity {


    GraphView graphView;
    LineGraphSeries<DataPoint> data;
    DatabaseHelper db;
    Integer n_coughing;
    int x = 0;
    int ch1,ch2;
    BluetoothAdapter bluetoothAdapter;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;

    private static final UUID my_UUID = UUID.fromString("0000FEAA-0000-1000-8000-00805f9b34fb");


    static final int DATA_RECEIVED = 5;


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
        //New version
        PyObject module = py.getModule("test");//Access the python module
        //get the predictions from the ml module
        PyObject Prediction = module.callAttr("evaluate",0);//access the evaluate function of the python module
        System.out.println(Prediction);


        //upload the result in the locale database
        if(Prediction.equals("coughing")){
            n_coughing++;//update the number of coughing

           DateTimeFormatter date_f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
           DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH:mm:ss");

           //get the time and the date
           String Date = LocalDateTime.now().format(date_f).toString();
           String Time = LocalDateTime.now().format(time_f).toString();
           //Insert the data and the time it has been registered
           db.insertData(Date,"coughing",Time);
        }

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            System.out.println(message.what);

            switch (message.what)
            {
                case 0:
                    break;
                case DATA_RECEIVED:
                    byte[] readBuff= (byte[]) message.obj;
                    String inp =new String(readBuff,0,message.arg1);
                    data.appendData(new DataPoint(x++, Double.parseDouble(inp)), true,100);
                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClass(){
            try {
                serverSocket = bluetoothAdapter
                        .listenUsingRfcommWithServiceRecord("WearableSensorApp",my_UUID );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what = CONNECTED;
                    handler.sendMessage(message);

                    BLEManager manager=new BLEManager(socket);
                    manager.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientClass (BluetoothDevice device1)
        {
            device=device1;

            try {
                socket=device.createRfcommSocketToServiceRecord(my_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what= CONNECTED;
                handler.sendMessage(message);

                BLEManager manager= new BLEManager(socket);
                manager.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }



    public class BLEManager extends Thread{
        private final BluetoothSocket bSocket;
        private final InputStream intputS;
        private final OutputStream outputS;

        public BLEManager(BluetoothSocket bluetoothSocket){
            bSocket= bluetoothSocket;
            InputStream tempI = null ;
            OutputStream tempO = null;

            try {
                tempI = bluetoothSocket.getInputStream();
                tempO = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            intputS = tempI;
            outputS = tempO;
        }

        @Override
        public void run() {
            byte [] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = intputS.read(buffer);
                    handler.obtainMessage(DATA_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}