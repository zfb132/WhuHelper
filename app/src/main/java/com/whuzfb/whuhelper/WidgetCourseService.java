package com.whuzfb.whuhelper;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.IBinder;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zfb15 on 2017/9/11.
 */

public class WidgetCourseService extends Service {
    // 点击循环显示课程的最大数量
    public static final int numCourseofShow=10;
    public int t=0;
    public int s=0;
    public String[] weekday;
    // 标注当前课程是否为今天
    public static boolean flagofToday[] =new boolean[numCourseofShow];
    public String temp[]=new String[numCourseofShow];

    @Override
    public void onCreate() {
        super.onCreate();
        String m="请先登录保存信息";
        try {
            m = getSQLData();
            //Log.d("+++++++++",m);
            m=m.replaceAll("null","无");
            weekday = m.split("\n\n\n");
        }catch (Exception e){
            e.printStackTrace();
        }

        RemoteViews views=new RemoteViews(this.getPackageName(),R.layout.widget_course);//远程视图
        ComponentName provider=new ComponentName(this, AppWidgetCourse.class);//提供者
        AppWidgetManager manager=AppWidgetManager.getInstance(this);//小部件管理器
        manager.updateAppWidget(provider, views);//更新

        long time=System.currentTimeMillis();
        Date date=new Date(time);
        SimpleDateFormat format=new SimpleDateFormat("E");
        Log.d("tag", weekday.length+"");
        switch (format.format(date)){
            case "周六":
                temp[t]="今天星期六，好好休息吧！\n点击查看明天课程";
                flagofToday[t]=true;
                t++;
            case "周日":
                temp[t]="今天星期日，好好休息吧！\n点击查看明天课程";
                flagofToday[t]=true;
                t++;
            case "周一":
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周一")){
                        temp[t]=weekday[i];
                        flagofToday[t]=true;
                        t++;
                    }
                }
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周二")){
                        temp[t]=weekday[i];
                        flagofToday[t]=false;
                        t++;
                    }
                }
                break;
            case "周二":
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周二")){
                        temp[t]=weekday[i];
                        flagofToday[t]=true;
                        t++;
                    }
                }
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周三")){
                        temp[t]=weekday[i];
                        flagofToday[t]=false;
                        t++;
                    }
                }
                break;
            case "周三":
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周三")){
                        temp[t]=weekday[i];
                        flagofToday[t]=true;
                        t++;
                    }
                }
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周四")){
                        temp[t]=weekday[i];
                        flagofToday[t]=false;
                        t++;
                    }
                }
                break;
            case "周四":
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周四")){
                        temp[t]=weekday[i];
                        flagofToday[t]=true;
                        t++;
                    }
                }
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周五")){
                        temp[t]=weekday[i];
                        flagofToday[t]=false;
                        t++;
                    }
                }
                break;
            case "周五":
                for(int i=0;i<weekday.length;i++){
                    if(weekday[i].contains("周五")){
                        temp[t]=weekday[i];
                        flagofToday[t]=true;
                        t++;
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        //unregisterReceiver(receiver);
        t=0;
        s=0;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteViews views=new RemoteViews(this.getPackageName(),R.layout.widget_course);//远程视图
        ComponentName provider=new ComponentName(this, AppWidgetCourse.class);//提供者
        AppWidgetManager manager=AppWidgetManager.getInstance(this);//小部件管理器
        manager.updateAppWidget(provider, views);//更新
        if(s>=numCourseofShow){
            s=0;
        }
        if(temp[s]==null){
            s=0;
        }
        // 不同日子颜色不同
        if(flagofToday[s]){
            views.setTextColor(R.id.tv_widget, Color.parseColor("#33ff00"));
        }else{
            views.setTextColor(R.id.tv_widget, Color.parseColor("#FF4C66C4"));
        }
        views.setTextViewText(R.id.tv_widget, temp[s]);

        s++;
        manager.updateAppWidget(provider, views);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 读取数据库内容
    public String getSQLData(){
        String temp="";
        String[] columnName={"课头号：","课程名：","课程类型：","学习类型：","授课学院：","教师：","专业：","学分：","课时：","上课时间：","备注：","状态："};
        DatabaseContext dbContext = new DatabaseContext(WidgetCourseService.this);
        SQLHelper dbHelper = new SQLHelper(dbContext,"courseInfo.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor cursor = db.rawQuery("select age,sex,class from student where name=?",
        // new String[]{""});
        // StudentInfo info = new StudentInfo();
        // info.setAge(cursor.getInt(0));
        // info.setSex(cursor.getString(1));
        // info.setWhichclass(cursor.getString(2));
        Cursor cursor = db.rawQuery("select * from course",null);
        Log.d("+++++++",""+cursor.getCount());
        while(cursor.moveToNext()){
            for(int i=1;i<=12;i++){
                temp = temp + columnName[i-1] + cursor.getString(i);
            }
            temp += "\n\n\n";
        }
        cursor.close();
        db.close();
        return temp;
    }
}