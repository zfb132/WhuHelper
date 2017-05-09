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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfb15 on 2017/5/9.
 */

public class TeachLogin extends Activity {
    private EditText et_check;
    private EditText et_user;
    private EditText et_pwd;
    private Spinner spinner_studyweb;
    private TextView tv_result;
    private Button btn_login;
    private Button btn_changecode;
    private Button btn_sourcecode;
    private ImageView img_checkcode;
    private Bitmap bm_checkCode;

    private String cookie="";
    private Map<String, String> cookies=new HashMap<>();
    private String token="";
    private static String URL_CHECKCODE="http://210.42.121.241/servlet/GenImg";
    private static String URL_LOGIN="http://210.42.121.241/servlet/Login";

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

    }

    //更新UI线程一般放在handler里面
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = "";
            val=data.getString("value");
            tv_result.setText(cookie+"\n"+val);
            img_checkcode.setImageBitmap(bm_checkCode);
        }
    };


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
                msg.setData(data);
                handler.sendMessage(msg);
            }
    };

    Runnable rb_login = new Runnable(){
        @Override
        public void run() {
            //网络相关操作均使用Thread
            // TODO: http request.
            //获取，cooking和表单属性，下面map存放post时的数据
            Map<String, String> post_params=new HashMap<>();
            post_params.put("id",et_user.getText().toString());
            post_params.put("pwd",getMd5Value(et_pwd.getText().toString()));
            post_params.put("xdvfb",et_check.getText().toString());
            Connection.Response res=null;
            Message msg = new Message();
            Bundle data = new Bundle();
            try {
                Connection con=Jsoup.connect(URL_LOGIN);
                con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                //设置cookie和post上面的map数据
                res=con.ignoreContentType(true).method(Connection.Method.POST).data(post_params).cookies(cookies).execute();
                data.putString("value",res.body());
            } catch (IOException e) {
                data.putString("value","未获取到数据");
                e.printStackTrace();
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void saveUpdateCookie(){
        SharedPreferences setinfo=getPreferences(Activity.MODE_PRIVATE);
        String username=setinfo.getString("STUDYNUM","");
        String password=setinfo.getString("PASSWORD","");
        cookie=setinfo.getString("COOKIE","");
        token=setinfo.getString("TOKEN","");
        et_user.setText(username);
        et_pwd.setText(password);
    }

    private void initView(){
        et_check = (EditText) findViewById(R.id.et_check);
        et_user=(EditText) findViewById(R.id.et_user);
        et_pwd=(EditText) findViewById(R.id.et_pwd);
        spinner_studyweb=(Spinner)findViewById(R.id.spinner_studyweb);
        btn_login = (Button) findViewById(R.id.btn_post);
        btn_changecode = (Button) findViewById(R.id.btn_web);
        btn_sourcecode = (Button) findViewById(R.id.btn_sourcecode);
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

    public void showError(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }

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

    public boolean checkSDCard() {
        //检测SD卡是否插入手机
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
