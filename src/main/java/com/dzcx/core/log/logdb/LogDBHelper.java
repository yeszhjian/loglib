package com.dzcx.core.log.logdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chen3 on 2017/9/14.
 */

public class LogDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "log_analysis.db";
    public static final String TABLE_NAME    = "t_client_log";
    public static final String ROW_TIME      = "time";
    public static final String ROW_MSG       = "msg";
    public static final String ROW_LOGID     = "logId";
    public static final String ROW_TYPE      = "type";

    public LogDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public LogDBHelper(Context context, int version) {
        this(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists t_client_log(id integer primary key autoincrement,type integer,time varchar(15),msg text,logId long);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
