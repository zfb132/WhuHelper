package com.whuzfb.whuhelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zfb15 on 2017/5/9.
 */

public class TeachLogin extends Activity {
    private EditText et_check;
    private EditText et_user;
    private EditText et_pwd;
    private Spinner spinner_studyweb;
    private TextView tv_result;
    private Button btn_post;
    private Button btn_changecode;
    private Button btn_sourcecode;
    private ImageView img_checkcode;
    private Bitmap bm_checkCode;

    private String cookie="";
    private String token="";
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


    }

    private void setListener(){
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //;
            }
        });
    }

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
        btn_post = (Button) findViewById(R.id.btn_post);
        btn_changecode = (Button) findViewById(R.id.btn_web);
        btn_sourcecode = (Button) findViewById(R.id.btn_sourcecode);
        tv_result = (TextView) findViewById(R.id.tv_result);
        img_checkcode = (ImageView) findViewById(R.id.img);
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
