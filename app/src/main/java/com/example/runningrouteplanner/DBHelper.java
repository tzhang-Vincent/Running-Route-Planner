package com.example.runningrouteplanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "db", null, 1);
        Log.d("g54mdp", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("g54mdp", "onCreateDBHelper");

        // create a recipe table
        db.execSQL("CREATE TABLE runningrouteplanner (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT ,"+
                "date DATE NOT NULL," +
                "startPoint VARCHAR NOT NULL," +
                "endPoint VARCHAR NOT NULL," +
                "distance FLOAT(10,2) NOT NULL" +
                ");");

        // insert two original data
        db.execSQL("INSERT INTO runningrouteplanner (date, startPoint, endPoint, distance) VALUES ('2019-11-08', '42.3482,-71.1394', '42.3565,-71.1498', 5);");
        db.execSQL("INSERT INTO runningrouteplanner (date, startPoint, endPoint, distance) VALUES ('2019-11-09', '42.3482,-71.1394', '42.3382,-71.1519', 3);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS runningrouteplanner");
        onCreate(db);
    }
}