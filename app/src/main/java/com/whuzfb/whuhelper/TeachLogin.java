package com.whuzfb.whuhelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zfb15 on 2017/5/9.
 */

public class TeachLogin extends Activity {

    //控件声明
    private EditText et_check;
    private EditText et_user;
    private EditText et_pwd;
    private Spinner spinner_studyweb;
    private TextView tv_result;
    private Button btn_login;
    private Button btn_changecode;
    private Button btn_course;
    private ImageView img_checkcode;
    private Bitmap bm_checkCode;

    //网站cookie等登录验证
    private String cookie="";
    private Map<String, String> cookies=new HashMap<>();
    private Map<String, String> headers=new HashMap<>();
    private String token="";
    private String timestamp="";

    //登录URL
    private static final String URL_HOST="http://210.42.121.241/";
    private static final String URL_CHECKCODE=URL_HOST+"servlet/GenImg";
    private static final String URL_LOGIN=URL_HOST+"servlet/Login";
    private static final String URL_COURSE=URL_HOST+"servlet/Svlt_QueryStuLsn";
    private static final String URL_SCORE=URL_HOST+"servlet/Svlt_QueryStuScore";

    private static final String REGEX_TOKEN="&csrftoken=([\\S]{36})";

    //对应函数public void changeTextDisplay(int num,String text)
    private static final int NUM_CHECKCODE=1;
    private static final int NUM_LOGIN=2;
    private static final int NUM_COURSE=3;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachlogin);
        //不自动弹出软键盘
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //初始化各View组件
        initView();
        //持久化并更新cookie与token
        saveUpdateCookie();
        //为应用在SD卡创建目录/sdcard/WhuHelper
        createdirs();
        setListener();
    }

    //为各种控件绑定监听器
    private void setListener(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(rb_login).start();
            }
        });
        btn_changecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_changecode.setText("看不清");
                new Thread(rb_getCheckCode).start();
            }
        });
        btn_course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(rb_getScore).start();
            }
        });
    }

    //更新UI线程一般放在handler里面
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = "";
            int typeName=0;
            val=data.getString("value");
            typeName=data.getInt("Type");
            changeTextDisplay(typeName,val);
        }
    };

    //更新UI显示
    public void changeTextDisplay(int num,String text){
        switch (num){
            case NUM_LOGIN:
                if(text.contains("码错误"))
                    tv_result.setText("用户名/密码/验证码错误！！！");
                else
                    tv_result.setText(cookie+"\n"+token+"\n"+text);
                break;
            case NUM_CHECKCODE:
                tv_result.setText(cookie);
                img_checkcode.setImageBitmap(bm_checkCode);
                break;
            case NUM_COURSE:
                tv_result.setText("congratulations\n"+text);
        }
    }

    //设置登录参数
    public Map<String,String> setLoginParams(){
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> post_params=new HashMap<>();
        post_params.put("id",et_user.getText().toString());
        post_params.put("pwd",getMd5Value(et_pwd.getText().toString()));
        post_params.put("xdvfb",et_check.getText().toString());
        return post_params;
    }

    //设置获取课程的URL参数
    public Map<String,String> setScoreParams(){
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> post_params=new HashMap<>();
        post_params.put("state","");
        post_params.put("term", "");
        post_params.put("learnType", "");
        post_params.put("scoreFlag", "");
        post_params.put("t", timestamp);
        post_params.put("csrftoken",token);
        /*
        try {
            post_params.put("term", URLEncoder.encode("下", "gb2312"));
            Log.d("TAG",URLEncoder.encode("下", "gb2312"));
            Log.d("TAG",URLEncoder.encode("下", "gbk"));
        } catch (UnsupportedEncodingException e) {
            post_params.put("term", "下");
            showError(e.toString());
        }
        */
        return post_params;
    }

    //正则匹配
    public String[] findStringInText(String text,String regex){
        Pattern patternName = Pattern.compile(regex);
        Matcher mName = patternName.matcher(text);
        int matchNum=mName.groupCount();
        if(!mName.find())
            return new String[]{"error"};
        String[] result=new String[matchNum];
        while(mName.find()) {
            for (int i = 1; i <= matchNum; i++) {
                result[i - 1] = mName.group(i);
            }
        }
        return result;
    }

    //更新缓存
    private void saveUpdateCookie(){
        SharedPreferences setinfo=getPreferences(Activity.MODE_PRIVATE);
        String username=setinfo.getString("STUDYNUM","");
        String password=setinfo.getString("PASSWORD","");
        cookie=setinfo.getString("COOKIE","");
        token=setinfo.getString("TOKEN","");
        et_user.setText(username);
        et_pwd.setText(password);
    }

    //初始化View
    private void initView(){
        et_check = (EditText) findViewById(R.id.et_check);
        et_user=(EditText) findViewById(R.id.et_user);
        et_pwd=(EditText) findViewById(R.id.et_pwd);
        spinner_studyweb=(Spinner)findViewById(R.id.spinner_studyweb);
        btn_login = (Button) findViewById(R.id.btn_post);
        btn_changecode = (Button) findViewById(R.id.btn_web);
        btn_course = (Button) findViewById(R.id.btn_course);
        tv_result = (TextView) findViewById(R.id.tv_result);
        img_checkcode = (ImageView) findViewById(R.id.img);
    }

    //获得任意字符串的MD5值
    public  String getMd5Value(String sSecret) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(sSecret.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();// 加密
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onStop() {
        //获取Shared Preference对象
        SharedPreferences setinfo=getPreferences(Activity.MODE_PRIVATE);
        //保存用户名和密码
        setinfo.edit()
                .putString("STUDYNUM",et_user.getText().toString())
                .putString("PASSWORD",et_pwd.getText().toString())
                .putString("COOKIE",cookie)
                .putString("TOKEN",token)
                .commit();
        super.onStop();
    }

    //对特定文件写入数据内容
    public void writeData(String filename,String string){
        try{
            //文件输出流，如果目标文件不存在，新建一个；如果已存在，默认覆盖
            FileOutputStream fileOutputStream=new FileOutputStream(filename);
            byte[] bytes=string.getBytes();
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showError(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            showError(e.toString());
        }
    }

    //Toast显示字符串
    public void showError(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }

    //在存储器创建目录
    public void createdirs(){
        if (checkSDCard()) {
            File recordPath = Environment.getExternalStorageDirectory();
            File path = new File(recordPath.getPath() + File.separator + "WhuHelper");
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    showError("创建目录失败，请检查权限！");
                    return;
                }
            }
            recordPath = path;
        } else {
            showError("SD卡未连接");
            return;
        }
    }

    //检查SD卡是否存在
    public boolean checkSDCard() {
        //检测SD卡是否插入手机
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }


    /*
    ***以下是网络相关操作
     */
    Runnable rb_getCheckCode = new Runnable(){
        @Override
        public void run() {
            //网络相关操作均使用Thread
            // TODO: http request.
            Connection.Response response=null;
            Message msg = new Message();
            Bundle data = new Bundle();
            try {
                Connection c=Jsoup.connect(URL_CHECKCODE).timeout(10000).ignoreContentType(true);
                response=c.execute();
                cookies=response.cookies();
                cookie=response.cookies().get("JSESSIONID");
                bm_checkCode = BitmapFactory.decodeByteArray(response.bodyAsBytes(), 0, response.bodyAsBytes().length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.putString("value","");
            data.putInt("Type",NUM_CHECKCODE);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    Runnable rb_login = new Runnable(){
        @Override
        public void run() {
            //网络相关操作均使用Thread
            Connection.Response res=null;
            Message msg = new Message();
            Bundle data = new Bundle();
            try {
                Connection con=Jsoup.connect(URL_LOGIN);
                con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                //设置cookie和post上面的map数据
                res=con.ignoreContentType(true).method(Connection.Method.POST).data(setLoginParams()).cookies(cookies).execute();
                data.putString("value",res.body());

                //正则表达式匹配出csrftoken
                token=findStringInText(res.body(),REGEX_TOKEN)[0];
                Log.d("TTT",token);
            } catch (IOException e) {
                data.putString("value","未获取到数据");
                e.printStackTrace();
            }
            data.putInt("Type",NUM_LOGIN);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    Runnable rb_getScore = new Runnable(){
        @Override
        public void run() {

            Calendar cd = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE%20MMM%20dd%20yyyy%20HH:mm:ss%20'GMT'+0800%20", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); // 设置时区为GMT
            timestamp= sdf.format(cd.getTime())+"(%D6%D0%B9%FA%B1%EA%D7%BC%CA%B1%BC%E4)";

            //网络相关操作均使用Thread
            Connection.Response res=null;
            Message msg = new Message();
            Bundle data = new Bundle();
            try {
                Connection con=Jsoup.connect(URL_SCORE);
                //con.header();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                headers.put("Content-Type","text/html;charset=gb2312");
                headers.put("Host","210.42.121.241");
                headers.put("Referer","http://210.42.121.241/stu/stu_score_parent.jsp");
                con.headers(headers);
                //设置cookie和post上面的map数据
                res=con.method(Connection.Method.GET).data(setScoreParams()).cookies(cookies).execute();
                data.putString("value",res.body());
            } catch (IOException e) {
                data.putString("value","未获取到数据");
                e.printStackTrace();
            }
            data.putInt("Type",NUM_COURSE);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };



    /*
    Runnable rb_getCourse = new Runnable(){
        @Override
        public void run() {
            //网络相关操作均使用Thread
            Connection.Response res=null;
            Message msg = new Message();
            Bundle data = new Bundle();
            try {
                Connection con=Jsoup.connect(URL_COURSE);
                //con.header();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                headers.put("Content-Type","text/html;charset=gb2312");
                headers.put("Host","210.42.121.241");
                headers.put("Referer","http://210.42.121.241/stu/stu_course_parent.jsp");
                con.headers(headers);
                //设置cookie和post上面的map数据
                res=con.method(Connection.Method.GET).data(setCourseParams()).cookies(cookies).execute();
                data.putString("value",res.body());
            } catch (IOException e) {
                data.putString("value","未获取到数据");
                e.printStackTrace();
            }
            data.putInt("Type",NUM_COURSE);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
    */

}
