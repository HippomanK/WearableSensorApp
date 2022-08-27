package no.nordicsemi.android.blinky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String ID = "id";
    public static final String DATE = "Date";
    public static final String TYPE = "type";
    public static final String TIME = "time";
    public static final String TEST = "test";

    public DatabaseHelper(Context context) {
        super(context, "Result.db", null, 1);
    }

    //Create local table
    @Override
    public void onCreate(SQLiteDatabase Db) {
        Db.execSQL("create Table " + TEST + "(id INTEGER PRIMARY KEY AUTOINCREMENT,Date Text,Result String)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase Db, int i, int i1) {
        Db.execSQL("Drop Table if exists " + TEST);
    }

    //Store the symptom type, date and time locally
    public boolean insertData(String Date,String Type,String Time){
        SQLiteDatabase Db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DATE,Date);
        contentValues.put(TIME,Time);
        contentValues.put(TYPE,Type);


        long inserted = Db.insert(TEST,null,contentValues);
        if(inserted==-1){
            return false;
        }else {
            return true;
        }
    }


    @Override
    public synchronized void close() {
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null){
            db.close();
            super.close();
        }

    }
}
