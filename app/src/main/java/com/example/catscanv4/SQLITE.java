package com.example.catscanv4;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SQLITE extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "predictions.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PREDICTIONS = "predictions";
    public static final String COLUMN_ID = "id";
        public static final String COLUMN_PREDICTION = "prediction";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PREDICTIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PREDICTION + " TEXT NOT NULL);";

    public SQLITE(Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);

    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREDICTIONS);
        onCreate(db);
    }
}
