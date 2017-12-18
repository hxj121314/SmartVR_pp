package com.martin.ads.vrlib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDataBase extends SQLiteOpenHelper {
    private SQLiteDatabase mdb;
    private final String CREATEbd = "create table matrix ("
            + "id integer primary key autoincrement,"
            + "columns1 real,"
            + "columns2 real,"
            + "columns3 real,"
            + "columns4 real)";
    private final String TBLname = "matrix";
    private final String DBname = "vrDataBase.db";

    public MyDataBase(Context context) {
        super(context, "vrDataBase.db", null, 1);
    }

    //插入数据
    public void insert(float[] m)
    {
        //获得SQLiteDatabase实例
        mdb = getWritableDatabase();
        Log.i("create_mdb","suc_insert");

        for(int i = 0; i < 4; i++){
            //装数据
            ContentValues cv = new ContentValues();
            cv.put("columns1", m[0+i*4]);
            cv.put("columns2", m[1+i*4]);
            cv.put("columns3", m[2+i*4]);
            cv.put("columns4", m[3+i*4]);
            //插入
            mdb.insert(TBLname, null, cv);
            cv.clear();
        }
        mdb.close();
    }

    //查询数据
    public float[] query(int id)
    {
        int i = id - 1;
        float[] m = new float[16];
        //获得SQLiteDatabase实例
        mdb = getWritableDatabase();
        Log.i("create_mdb","suc_query");
        //查询获得Cursor
        Cursor c = mdb.query(TBLname, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            do {
                m[0 + i * 4] = c.getFloat(c.getColumnIndex("columns1"));
                m[1 + i * 4] = c.getFloat(c.getColumnIndex("columns2"));
                m[2 + i * 4] = c.getFloat(c.getColumnIndex("columns3"));
                m[3 + i * 4] = c.getFloat(c.getColumnIndex("columns4"));
                i++;
            } while (c.moveToNext() && (i<4));
        }
        c.close();
        mdb.close();
        return m;
    }

    //删除数据
    public void delet()
    {
        //获得SQLiteDatabase实例
        mdb = getWritableDatabase();
        Log.i("create_mdb","suc_delet");
        //执行删除
        mdb.delete(TBLname, null, null);
        Log.i("create_mdb","mdb_delet");
        mdb.close();
    }

    //关闭数据库
    public void colseDataBase()
    {
        if(mdb != null)
        {
            mdb.close();
            Log.i("create_mdb","close");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        this.mdb = db;
        mdb.execSQL(CREATEbd);
        Log.i("creat_mdb","yes");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
