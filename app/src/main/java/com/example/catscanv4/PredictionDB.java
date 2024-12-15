package com.example.catscanv4;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PredictionDB {
    private SQLITE dbHelper;

    public PredictionDB(Context context){
        dbHelper = new SQLITE(context);
    }

    public void insertPrediction(String prediction){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(SQLITE.COLUMN_PREDICTION, prediction);
        db.insert(SQLITE.TABLE_PREDICTIONS,null,values);

        db.close();
    }

    public void resetLogs(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLITE.TABLE_PREDICTIONS,null, null);

        db.close();

    }

    public List<String> fetchDB(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<String> predictions = new ArrayList<>();
        String[] columns = {SQLITE.COLUMN_PREDICTION};
        Cursor cursor = db.query(SQLITE.TABLE_PREDICTIONS,columns,null,null,null,null,null);

        if(cursor != null){
            while(cursor.moveToNext()){
                @SuppressLint("Range") String prediction = cursor.getString(cursor.getColumnIndex(SQLITE.COLUMN_PREDICTION));
                predictions.add(prediction);

            }
            cursor.close();
        }
        db.close();
        return predictions;
    }
}
