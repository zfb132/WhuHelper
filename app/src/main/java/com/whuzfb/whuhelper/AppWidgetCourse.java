package com.whuzfb.whuhelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by zfb15 on 2017/9/11.
 */

public class AppWidgetCourse extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        context.startService(new Intent(context,WidgetCourseService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context,AppWidgetCourse.class);
        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget_course);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,new Intent(context,WidgetCourseService.class),0);
        views.setOnClickPendingIntent(R.id.tv_widget,pendingIntent);
        //context.startService();
        manager.updateAppWidget(provider, views);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d("WhuHelper: 桌面挂件提供者","onDeleted方法调用了");
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WhuHelper: 桌面挂件提供者","onEnabled方法调用了");
        //启动MyService
        //Intent intent = new Intent(context,MyService.class);
        //context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("WhuHelper: 桌面挂件提供者","onDisabled方法调用了");
        //停止MyService
        //Intent intent = new Intent(context,MyService.class);。
        //context.stopService(intent);
    }

}