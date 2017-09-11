package com.whuzfb.whuhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zfb15 on 2017/9/9.
 */

public class SQLHelper extends SQLiteOpenHelper {
    public static final String TAG="DATABASE";
    //必须要有构造函数
    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 当第一次创建数据库的时候，调用该方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "create table if not exists course(" +
                "id int(8) null,"+
                "courseID varchar(15) not null,"+
                "courseName varchar(50) not null,"+
                "courseType varchar(10) not null,"+
                "studyType varchar(10) null default '普通',"+
                "college varchar(20) null default '无',"+
                "teacher varchar(10) null default '未知',"+
                "profession varchar(30) null default '未知',"+
                "credit varchar(10) not null,"+
                "timeLast varchar(5) null default '0',"+
                "time varchar(100) null default '未知',"+
                "note varchar(150) null default '无',"+
                "state varchar(10) null default '未结算',"+
                "primary key(id)"+
                ");";
        //输出创建数据库的日志信息
        Log.i(TAG, "创建数据库表------------->");
        //execSQL函数用于执行SQL语句
        db.execSQL(sql);
    }

    //当更新数据库的时候执行该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //输出更新数据库的日志信息
        Log.i(TAG, "update Database------------->");
    }


}

/*
String sql =
          "create table if not exists course(" +
          "id int(8) null,"+
          "courseID varchar(15) not null,"+
          "courseName varchar(50) not null,"+
          "courseType varchar(10) not null,"+
          "studyType varchar(10) null default '普通',"+
          "college varchar(20) null default '无',"+
          "teacher varchar(10) null default '未知',"+
          "profession varchar(30) null default '未知',"+
          "credit varchar(10) not null,"+
          "timeLast varchar(5) null default '0',"+
          "time varchar(100) null default '未知',"+
          "note varchar(150) null default '无',"+
          "state varchar(10) null default '未结算',"+
          "primary key(id)"+
          ")default charset=utf8;";

*/
