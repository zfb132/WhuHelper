package com.whuzfb.whuhelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

import sign.WebURL;

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
    private Button btn_score;
    private Button btn_course;
    private ImageView img_checkcode;
    private Bitmap bm_checkCode;

    //网站cookie等登录验证
    private String cookie="";
    private Map<String, String> cookies=new HashMap<>();
    private Map<String, String> headers=new HashMap<>();
    private String token="";

    //登录URL
    private static final String URL_HOST="http://210.42.121.241/";
    private static final String URL_CHECKCODE=URL_HOST+"servlet/GenImg";
    private static final String URL_LOGIN=URL_HOST+"servlet/Login";
    private static final String URL_COURSE=URL_HOST+"servlet/Svlt_QueryStuLsn";
    private static final String URL_SCORE=URL_HOST+"servlet/Svlt_QueryStuScore";

    private static final String REGEX_TOKEN="&csrftoken=([\\S]{36})";

    //对应函数public void changeTextDisplay(int num,String text)
    private static final int FLAG_CHECKCODE=1;
    private static final int FLAG_LOGIN=2;
    private static final int FLAG_COURSE=3;
    private static final int FLAG_SCORE=4;
    private static final int NUM_COLOFCOURSE=12;
    private static final int NUM_COLOFSCORE=10;
    //此处num_course是当前学年课程数+1（因为有一个表头）
    private static int num_course=1;
    private static int num_score=1;

    private static String directory_root="/sdcard";

    private static final String DIRECTORY="WhuHelper/course";



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
        //为应用在SD卡创建目录/sdcard/WhuHelper/course
        createdirs();
        setListener();


    }

    //为各种控件绑定监听器
    private void setListener(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(runLogin()).start();
            }
        });
        img_checkcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {new Thread(runGetCheckCode()).start();
            }
        });
        btn_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(runGetScore()).start();
            }
        });
        btn_course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {new Thread(runGetCourse()).start();
            }
        });
    }

    //更新UI线程一般放在handler里面
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String str = "";
            int typeName=0;
            str=data.getString("value");
            typeName=data.getInt("Type");

            if(typeName==FLAG_COURSE){
                writeData(directory_root+File.separator+DIRECTORY+File.separator+"course.html",str);
                str=getCourseInfo(str);
            }
            if(typeName==FLAG_SCORE){
                writeData(directory_root+File.separator+DIRECTORY+File.separator+"score.html",str);
                str=getScoreInfo(str);
            }
            changeTextDisplay(typeName,str);
        }
    };


    //更新UI显示
    public void changeTextDisplay(int num,String text){
        switch (num){
            case FLAG_LOGIN:
                if(text.contains("码错误"))
                    tv_result.setText("用户名/密码/验证码错误！！！");
                else{
                    tv_result.setText(cookie+"\n"+token+"\n"+text);
                    // 成功登陆保存cookie
                    //saveUpdateCookie();
                }

                break;
            case FLAG_CHECKCODE:
                tv_result.setText(cookie);
                img_checkcode.setImageBitmap(bm_checkCode);
                break;
            case FLAG_COURSE:
                tv_result.setText("正在获取课程表数据！\n\n"+text);
            case FLAG_SCORE:
                tv_result.setText("正在获取成绩！\n\n"+text);
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

    //设置获取分数的URL参数
    public Map<String,String> setScoreParams(){
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> post_params=new HashMap<>();
        // 0 全部学年
        post_params.put("year","0");
        // "" 上下学期
        post_params.put("term", "");
        // "" 所有类型
        post_params.put("learnType", "");
        // 0 全部；1 及格；2 不及格；3 未出成绩；4 已出成绩
        post_params.put("scoreFlag", "0");
        post_params.put("csrftoken",token);

        return post_params;
    }

    //设置获取课程的URL参数
    public Map<String,String> setCourseParams(){
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> post_params=new HashMap<>();
        post_params.put("state","");
        post_params.put("year", "2017");
        post_params.put("action", "normalLsn");
        post_params.put("csrftoken",token);
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
        btn_score = (Button) findViewById(R.id.btn_web);
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
            directory_root=Environment.getExternalStorageDirectory().getPath();
            File path = new File(directory_root + File.separator + DIRECTORY);
            Log.d("WhuHelper: 文件路径",path.toString());
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    showError("创建目录失败，请检查权限！");
                    return;
                }
            }
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

    // 解析课程表的网页获取各科目详细信息
    // 将infoCourse的内容存储到数据库SQLite以备使用
    public String getCourseInfo(String str){
        Document doc= Jsoup.parse(str);
        Elements trs=doc.getElementsByTag("tr");
        num_course=trs.size();
        Elements[] tr=new Elements[num_course];
        String[][] infoCourse=new String[num_course][NUM_COLOFCOURSE];
        String[] columnNames=new String[NUM_COLOFCOURSE];

        Log.d("WhuHelper: 所有课程信息",trs.toString());
        writeData(directory_root+File.separator+DIRECTORY+File.separator+"coursetrs.txt",trs.toString());
        str="";
        int i=0,t;
        for(Element tr_temp:trs){
            tr[i++]=tr_temp.children();
            //str+=tr.toString();
            Log.d("WhuHelper: 某一门课",tr.toString());
        }
        for(i=0;i<num_course;i++){
            t=0;
            if(i>1){
                str+="\n********下一门课********\n";
            }
            for(Element td:tr[i]){
                //i=0，即第一行数据，是课程表的每一列的列名
                if(i==0){
                    //由于列只有12行
                    if(t<NUM_COLOFCOURSE){
                        infoCourse[0][t]=td.text();
                        columnNames[t]=infoCourse[0][t];
                        t++;
                        continue;
                    }else
                        break;
                }
                //第10列数据就是上课时间地点安排，有子节点
                if(t==9){
                    Elements temp=td.children();
                    for(Element td_note:temp){
                        infoCourse[i][t]=td_note.text()+"\n";
                    }
                }else{
                    infoCourse[i][t]=td.text()+"\n";
                }
                str=str+columnNames[t]+" : "+infoCourse[i][t];
                Log.d("WhuHelper: 课程信息",infoCourse[i][t]);
                t++;
            }
        }

        // 当获取到数据后再写入数据库
        if(str!=""){
            // 因为获得的num_course包含表头
            if(getSQLNumOfCourse()<(num_course-1)){
                //将信息写入数据库
                DatabaseContext dbContext = new DatabaseContext(TeachLogin.this);
                SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
                //得到一个可写的数据库
                SQLiteDatabase db =dbHelper.getWritableDatabase();
                //由于第一行是表头而不是课程信息，故从1开始
                for(int n=1;n<num_course;n++){
                    //db.execSQL("insert into course(id,courseID,courseName,courseType,studyType,college,teacher,profession,credit,timeLast,time,note,state) values ("+(n-1)+","+infoCourse[n][0]+","+infoCourse[n][1]+","+infoCourse[n][2]+","+infoCourse[n][3]+","+infoCourse[n][4]+","+infoCourse[n][5]+","+infoCourse[n][6]+","+infoCourse[n][7]+","+infoCourse[n][8]+","+infoCourse[n][9]+","+infoCourse[n][10]+","+infoCourse[n][11]+");");
                    db.execSQL("insert into course(id,courseID,courseName,courseType,studyType,college,teacher,profession,credit,timeLast,time,note,state) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{n-1,infoCourse[n][0],infoCourse[n][1],infoCourse[n][2],infoCourse[n][3],infoCourse[n][4],infoCourse[n][5],infoCourse[n][6],infoCourse[n][7],infoCourse[n][8],infoCourse[n][9],infoCourse[n][10],infoCourse[n][11]});
                    // 其实有更方便的方法如下
                    // db.insert()
                    //Log.d("----------","zhengzai写入数据库");
                }
                db.close();
                Log.d("----------","写入数据库");
            }else{
                showError("数据库内容未更新，因为课程已保存");
            }
        }else{
            showError("未获取到课程信息");
        }

        //返回课程相关数据显示在TextView
        return str;
    }

    // 读取course数据表内容长度
    public int getSQLNumOfCourse(){
        int num=0;
        DatabaseContext dbContext = new DatabaseContext(TeachLogin.this);
        SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor cursor = db.rawQuery("select age,sex,class from student where name=?",
        // new String[]{""});
        // StudentInfo info = new StudentInfo();
        // info.setAge(cursor.getInt(0));
        // info.setSex(cursor.getString(1));
        // info.setWhichclass(cursor.getString(2));
        Cursor cursor = db.rawQuery("select id from course",null);
        num=cursor.getCount();
        Log.d("WhuHelper: 课程数据个数",""+num);
        //while(cursor.moveToNext()){
        //    temp += cursor.getString(2)+"\n";
        //}
        cursor.close();
        db.close();
        return num;
    }

    // 读取course数据表内容长度
    public int getSQLNumOfScore(){
        int num=0;
        DatabaseContext dbContext = new DatabaseContext(TeachLogin.this);
        SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor cursor = db.rawQuery("select age,sex,class from student where name=?",
        // new String[]{""});
        // StudentInfo info = new StudentInfo();
        // info.setAge(cursor.getInt(0));
        // info.setSex(cursor.getString(1));
        // info.setWhichclass(cursor.getString(2));
        Cursor cursor = db.rawQuery("select id from score",null);
        num=cursor.getCount();
        Log.d("WhuHelper: 成绩数据个数",""+num);
        //while(cursor.moveToNext()){
        //    temp += cursor.getString(2)+"\n";
        //}
        cursor.close();
        db.close();
        return num;
    }

    public String getScoreInfo(String str){
        Document doc= Jsoup.parse(str);
        Elements trs=doc.getElementsByTag("tr");

        num_score=trs.size();
        Elements[] tr=new Elements[num_score];
        String[][] infoCourse=new String[num_score][NUM_COLOFSCORE];
        String[] columnNames=new String[NUM_COLOFSCORE];

        Log.d("WhuHelper: 所有成绩信息",trs.toString());
        writeData(directory_root+File.separator+DIRECTORY+File.separator+"scoretrs.txt",trs.toString());
        str="";
        int i=0,t;
        for(Element tr_temp:trs){
            tr[i++]=tr_temp.children();
            //str+=tr.toString();
            Log.d("WhuHelper: 某一门课成绩",tr.toString());
        }
        for(i=0;i<num_score;i++){
            t=0;
            if(i>1){
                str+="\n********下一门课********\n";
            }
            for(Element td:tr[i]){
                // 由于实际有11列数据，最后一列不需要
                if(t==10)
                    break;
                //i=0，即第一行数据，是课程表的每一列的列名
                if(i==0){
                    //由于列只有12行
                    if(t<NUM_COLOFSCORE){
                        infoCourse[0][t]=td.text();
                        columnNames[t]=infoCourse[0][t];
                        t++;
                        continue;
                    }else
                        break;
                }
                infoCourse[i][t]=td.text()+"\n";
                str=str+columnNames[t]+" : "+infoCourse[i][t];
                t++;
            }
        }

        // 当获取到数据后再写入数据库
        if(str!=""){
            int oldnum=1;
            oldnum=getSQLNumOfScore();
            // 因为获得的num_score包含表头
            if((num_score-1)>oldnum){
                //将信息写入数据库
                DatabaseContext dbContext = new DatabaseContext(TeachLogin.this);
                SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
                //得到一个可写的数据库
                SQLiteDatabase db =dbHelper.getWritableDatabase();
                //避免每次从网页获得的成绩排序不同而增加判断语句
                db.execSQL("delete from score");
                //由于第一行是表头而不是课程信息，故从1开始
                for(int n=1;n<num_score;n++){
                    db.execSQL("insert into score(id,courseID,courseName,courseType,credit,teacher,college,studyType,year,term,score) values(?,?,?,?,?,?,?,?,?,?,?)",new Object[]{n-1,infoCourse[n][0],infoCourse[n][1],infoCourse[n][2],infoCourse[n][3],infoCourse[n][4],infoCourse[n][5],infoCourse[n][6],infoCourse[n][7],infoCourse[n][8],infoCourse[n][9]});
                }
                db.close();
                Log.d("WhuHelper: ","写入数据库");
            }else{
                showError("数据库内容未更新，因为成绩已保存");
            }
        }else{
            showError("未获取到成绩信息");
        }

        //返回成绩相关数据显示在TextView
        return str;
    }

    /*
    ***以下是网络相关操作
     */
    public Runnable runGetCheckCode(){
        Runnable rb_getCheckCode = new Runnable(){
            @Override
            public void run() {
                //网络相关操作均使用Thread
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
                data.putInt("Type",FLAG_CHECKCODE);
                msg.setData(data);
                handler.sendMessage(msg);

            }
        };
        return rb_getCheckCode;
    }

    public Runnable runLogin(){
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
                    Log.d("WhuHelper: token",token);
                } catch (IOException e) {
                    data.putString("value","未获取到数据");
                    e.printStackTrace();
                }
                data.putInt("Type",FLAG_LOGIN);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };
        return rb_login;
    }

    public Runnable runGetScore(){
        Runnable rb_getScore = new Runnable(){
            @Override
            public void run() {
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
                data.putInt("Type",FLAG_SCORE);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };
        return rb_getScore;
    }

    public Runnable runGetCourse(){
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
                    con.headers(headers);
                    //设置cookie和post上面的map数据
                    res=con.method(Connection.Method.GET).data(setCourseParams()).cookies(cookies).execute();
                    data.putString("value",res.body());
                } catch (IOException e) {
                    data.putString("value","未获取到数据");
                    e.printStackTrace();
                }
                Log.d("WhuHelper: 课程页面",data.toString());
                data.putInt("Type",FLAG_COURSE);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };
        return rb_getCourse;
    }
}

/*
* Jsoup的一些应用
                Elements tables=doc.getElementsByTag("table");
                Elements trs=null;
                Log.d("EEEEEEE",tables.toString());
                for(Element table:tables){
                    trs=table.children();
                }
                writeData(DIRECTORY_ROOT+File.separator+DIRECTORY+File.separator+"trs.txt",trs.toString());
                String temp="";
                for(Element tr:trs){
                    Log.d("EEEEEEEWWWWW",tr.toString());
                    temp+=tr.toString();
                }
                writeData(DIRECTORY_ROOT+File.separator+DIRECTORY+File.separator+"tr.html",temp);
                str=temp;
*/
/*
//由于Jsoup在url包含中文参数时编码默认UTF-8，因此无法传递中文参数
Calendar cd = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("EEE%20MMM%20dd%20yyyy%20HH:mm:ss%20'GMT'+0800%20", Locale.US);
sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); // 设置时区为GMT
timestamp= sdf.format(cd.getTime())+"(%D6%D0%B9%FA%B1%EA%D7%BC%CA%B1%BC%E4)";
*/