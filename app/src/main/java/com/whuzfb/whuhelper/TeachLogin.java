package com.whuzfb.whuhelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.column.ColumnInfo;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnClickListener;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zfb15 on 2017/5/9.
 */

public class TeachLogin extends Fragment {

    //控件声明
    private EditText et_check;
    private EditText et_user;
    private EditText et_pwd;
    private TextView tv_time;
    private Button btn_login;
    private Button btn_score;
    private Button btn_course;
    private ImageView img_checkcode;
    private Bitmap bm_checkCode;
    private SmartTable table=null;

    private Context context=null;
    private FragmentActivity fragmentActivity=null;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取上下文用于其他函数
        context=getActivity();
        fragmentActivity=getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teachlogin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //不自动弹出软键盘
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //初始化各View组件
        initView(view);
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
                //new Thread(runGetScore()).start();
                Column<Integer> column1 = new Column<>("ID", "id");
                //设置列自动排序
                //column1.setAutoCount(true);
                Column<String> column2 = new Column<>("课程", "courseName");column2.setFixed(true);
                Column<String> column3 = new Column<>("分数", "score");
                Column<String> column4 = new Column<>("课程类型", "courseType");
                Column<String> column5 = new Column<>("学分", "credit");
                Column<String> column6 = new Column<>("老师", "teacher");
                Column<String> column7 = new Column<>("学院", "college");
                Column<String> column8 = new Column<>("学习类型", "studyType");
                Column<String> column9 = new Column<>("年", "year");
                Column<String> column10 = new Column<>("学期", "term");
                Column<String> column11 = new Column<>("课程ID", "courseID");
                List<ScoreInfo> scoreList=getSQLScoreData();
                //表格数据 datas是需要填充的数据
                TableData<ScoreInfo> tableData = new TableData<>("成绩单",scoreList,column1,column2,column3,column4,column5,column6,column7,column8,column9,column10,column11);
                // 设置排序的列
                tableData.setSortColumn(column1);
                table.setZoom(true,3,0.2f);
                initTable();
                table.setTableData(tableData);
            }
        });
        btn_course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new Thread(runGetCourse()).start();
                Column<Integer> column1 = new Column<>("ID", "id");
                Column<String> column2 = new Column<>("课程", "courseName");column2.setFixed(true);
                Column<String> column3 = new Column<>("课程类型", "courseType");
                Column<String> column4 = new Column<>("学习类型", "studyType");
                Column<String> column5 = new Column<>("学院", "college");
                Column<String> column6 = new Column<>("老师", "teacher");
                Column<String> column7 = new Column<>("专业", "profession");
                Column<String> column8 = new Column<>("学分", "credit");
                Column<String> column9 = new Column<>("学时", "timeLast");
                Column<String> column10 = new Column<>("时间", "time");
                Column<String> column11 = new Column<>("备注", "note");
                Column<String> column12 = new Column<>("状态", "state");
                Column<String> column13 = new Column<>("课程ID", "courseID");
                List<CourseInfo> courseList=getSQLCourseData();
                //表格数据 datas是需要填充的数据
                TableData<CourseInfo> tableData = new TableData<>("课程信息",courseList,column1,column2,column3,column4,column5,column6,column7,column8,column9,column10,column11,column12,column13);
                // 设置排序的列
                tableData.setSortColumn(column1);
                table.setZoom(true,3,0.2f);
                initTable();
                table.setTableData(tableData);
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
                saveCourseInfo(str);
            }
            if(typeName==FLAG_SCORE){
                writeData(directory_root+File.separator+DIRECTORY+File.separator+"score.html",str);
                saveScoreInfo(str);
            }
            changeTextDisplay(typeName,str);
        }
    };


    //更新UI显示
    public void changeTextDisplay(int num,String text){
        switch (num){
            case FLAG_LOGIN:
                if(text.contains("码错误"))
                    showTips("用户名/密码/验证码错误！！！");
                else{
                    showTips("登录成功,正在获取成绩和课程信息");
                    //Log.d("TeachLogin：",cookie+"\n"+token+"\n"+text);
                    // 成功登陆更新时间
                    updateFormatTime();
                    //saveUpdateCookie();
                    //自动获取成绩和课程表存入数据库
                    new Thread(runGetScore()).start();
                    new Thread(runGetCourse()).start();
                }

                break;
            case FLAG_CHECKCODE:
                //tv_result.setText(cookie);
                img_checkcode.setImageBitmap(bm_checkCode);
                break;
            case FLAG_COURSE:
                //tv_result.setText("正在获取课程表数据！\n\n"+text);
            case FLAG_SCORE:
                //tv_result.setText("正在获取成绩！\n\n"+text);
                break;
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
        SharedPreferences setinfo=fragmentActivity.getPreferences(Activity.MODE_PRIVATE);
        String username=setinfo.getString("STUDYNUM","");
        String password=setinfo.getString("PASSWORD","");
        cookie=setinfo.getString("COOKIE","");
        token=setinfo.getString("TOKEN","");
        et_user.setText(username);
        et_pwd.setText(password);
    }

    //初始化View
    private void initView(View v){
        et_check = (EditText) v.findViewById(R.id.et_check);
        et_user=(EditText) v.findViewById(R.id.et_user);
        et_pwd=(EditText) v.findViewById(R.id.et_pwd);
        btn_login = (Button) v.findViewById(R.id.btn_post);
        btn_score = (Button) v.findViewById(R.id.btn_web);
        btn_course = (Button) v.findViewById(R.id.btn_course);
        //tv_result = (TextView) v.findViewById(R.id.tv_result);
        tv_time=(TextView)v.findViewById(R.id.tv_time_lastupdate);
        img_checkcode = (ImageView) v.findViewById(R.id.img);
        table=(SmartTable)v.findViewById(R.id.table);
        SharedPreferences setinfo=fragmentActivity.getPreferences(Activity.MODE_PRIVATE);
        String lastupdate=setinfo.getString("LASTUPDATE","0");
        tv_time.setText(lastupdate);
    }

    // 初始化SmartTable配置
    private void initTable(){
        // 设置表头字体
        TableConfig config=table.getConfig();
        config.setTableTitleStyle(new FontStyle(getActivity(),23,getResources().getColor(R.color.colorTableTitle)).setAlign(Paint.Align.CENTER));
        // 设置cell字体
        config.setContentStyle(new FontStyle(getActivity(),20,getResources().getColor(R.color.colorTableContent)).setAlign(Paint.Align.CENTER));
        // 设置单元格背景色
        table.getConfig().setContentCellBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
            @Override
            public int getBackGroundColor(CellInfo cellInfo) {
                if(cellInfo.row %2 ==0) {
                    return ContextCompat.getColor(getActivity(), R.color.content_bg);
                }
                return TableConfig.INVALID_COLOR;
            }
        });
        // 设置列标题字体
        config.setColumnTitleStyle(new FontStyle(getActivity(),20,getResources().getColor(R.color.colorPrimary)).setAlign(Paint.Align.CENTER));
        // 设置不显示左边和上边的序列
        config.setShowXSequence(false);
        config.setShowYSequence(false);
        // 设置列点击事件
        table.setOnColumnClickListener(new OnColumnClickListener() {
            @Override
            public void onClick(ColumnInfo columnInfo) {
                table.setSortColumn(columnInfo.column, !columnInfo.column.isReverseSort());
            }
        });
    }

    // 显示当前时间字符串
    public void updateFormatTime(){
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); // 设置时区为GMT
        String time=sdf.format(cd.getTime());
        tv_time.setText(time);
        // 持久化更新时间
        SharedPreferences setinfo=fragmentActivity.getPreferences(Activity.MODE_PRIVATE);
        //保存最近更新时间
        setinfo.edit().putString("LASTUPDATE",time).commit();
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
    public void onStop() {
        //获取Shared Preference对象
        SharedPreferences setinfo=fragmentActivity.getPreferences(Activity.MODE_PRIVATE);
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
            showTips(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            showTips(e.toString());
        }
    }

    //Toast显示字符串
    public void showTips(String str) {
        Toast toast = Toast.makeText(fragmentActivity, str, Toast.LENGTH_LONG);
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
                    showTips("创建目录失败，请检查权限！");
                    return;
                }
            }
        } else {
            showTips("SD卡未连接");
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
    public void saveCourseInfo(String str){
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
                DatabaseContext dbContext = new DatabaseContext(context);
                SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
                //得到一个可写的数据库
                SQLiteDatabase db =dbHelper.getWritableDatabase();
                //避免每次从网页获得的课程排序不同而增加判断语句
                db.execSQL("delete from course");
                //由于第一行是表头而不是课程信息，故从1开始
                for(int n=1;n<num_course;n++){
                    //db.execSQL("insert into course(id,courseID,courseName,courseType,studyType,college,teacher,profession,credit,timeLast,time,note,state) values ("+(n-1)+","+infoCourse[n][0]+","+infoCourse[n][1]+","+infoCourse[n][2]+","+infoCourse[n][3]+","+infoCourse[n][4]+","+infoCourse[n][5]+","+infoCourse[n][6]+","+infoCourse[n][7]+","+infoCourse[n][8]+","+infoCourse[n][9]+","+infoCourse[n][10]+","+infoCourse[n][11]+");");
                    db.execSQL("insert into course(id,courseID,courseName,courseType,studyType,college,teacher,profession,credit,timeLast,time,note,state) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{n-1,infoCourse[n][0],infoCourse[n][1],infoCourse[n][2],infoCourse[n][3],infoCourse[n][4],infoCourse[n][5],infoCourse[n][6],infoCourse[n][7],infoCourse[n][8],infoCourse[n][9],infoCourse[n][10],infoCourse[n][11]});
                    // 其实有更方便的方法如下
                    // db.insert()
                    //Log.d("----------","zhengzai写入数据库");
                    showTips("已保存课程信息");
                }
                db.close();
                Log.d("----------","写入数据库");
            }else{
                showTips("数据库内容未更新，因为课程已保存");
            }
        }else{
            showTips("未获取到课程信息");
        }
    }

    // 读取course数据表内容长度
    public int getSQLNumOfCourse(){
        int num=0;
        DatabaseContext dbContext = new DatabaseContext(context);
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
        DatabaseContext dbContext = new DatabaseContext(context);
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

    public void saveScoreInfo(String str){
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
                DatabaseContext dbContext = new DatabaseContext(context);
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
                showTips("已保存成绩信息");
            }else{
                showTips("数据库内容未更新，因为成绩已保存");
            }
        }else{
            showTips("未获取到成绩信息");
        }
    }

    // 读取课程数据表内容
    public List<CourseInfo> getSQLCourseData(){
        List<CourseInfo> list=new ArrayList<>();
        DatabaseContext dbContext = new DatabaseContext(fragmentActivity);
        SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from course",null);
        Log.d("WhuHelper: 获取到课程个数",""+cursor.getCount());
        while(cursor.moveToNext()){
            CourseInfo info=new CourseInfo(cursor.getInt(0),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(11),cursor.getString(12),cursor.getString(1));
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    // 读取成绩数据表内容
    public List<ScoreInfo> getSQLScoreData(){
        List<ScoreInfo> list=new ArrayList<>();
        DatabaseContext dbContext = new DatabaseContext(fragmentActivity);
        SQLHelper dbHelper = new SQLHelper(dbContext,"study.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from score",null);
        Log.d("WhuHelper: 获取到成绩个数",""+cursor.getCount());
        while(cursor.moveToNext()){
            ScoreInfo info=new ScoreInfo(cursor.getInt(0),cursor.getString(2),cursor.getString(10),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(1));
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
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