package sms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.whuzfb.whuhelper.R;


/**
 * Created by zfb15 on 2017/8/7.
 */

public class EditSms extends AppCompatActivity {
    private Button btn_write=null;
    private Button btn_recover=null;
    private EditText edt_telenum=null;
    private EditText edt_content=null;
    private String defaultSmsPkgName="";
    private String myPkgName="";
    private boolean isDefault=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        btn_write=(Button)findViewById(R.id.btn_write);
        btn_recover=(Button)findViewById(R.id.btn_recover);
        edt_telenum=(EditText)findViewById(R.id.edt_telenum);
        edt_content=(EditText)findViewById(R.id.edt_content);

        defaultSmsPkgName= Telephony.Sms.getDefaultSmsPackage(this);
        myPkgName=this.getPackageName();

        if(Build.VERSION.SDK_INT>=19){
            if(!defaultSmsPkgName.equals(myPkgName)){
                //如果这个App不是默认的Sms App，则修改成默认的SMS APP
                //因为从Android 4.4开始，只有默认的SMS APP才能对SMS数据库进行处理
                Intent intent=new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,myPkgName);
                startActivity(intent);
            }
            if(myPkgName.equals(Telephony.Sms.getDefaultSmsPackage(EditSms.this))){
                isDefault=true;
            }
        }else{
            btn_recover.setVisibility(View.INVISIBLE);
        }

        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeSms();
            }
        });
        btn_recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=19){
                    setDefault();
                }
            }
        });
    }

    public void writeSms(){
        //对短信数据库进行处理
        ContentResolver resolver=getContentResolver();
        ContentValues values=new ContentValues();
        values.put(Telephony.Sms.ADDRESS,edt_telenum.getText().toString());
        values.put(Telephony.Sms.DATE, System.currentTimeMillis());
        long dateSent=System.currentTimeMillis()-5000;
        values.put(Telephony.Sms.DATE_SENT,dateSent);
        values.put(Telephony.Sms.READ,false);
        values.put(Telephony.Sms.SEEN,false);
        values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE);
        values.put(Telephony.Sms.BODY, edt_content.getText().toString());
        values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX);
        resolver.insert(Telephony.Sms.CONTENT_URI,values);
        Log.d("AAAAAA","成功写入");
        Toast.makeText(EditSms.this,"成功写入",Toast.LENGTH_SHORT).show();
    }

    public void setDefault(){
        if(isDefault){
            Intent intent=new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,defaultSmsPkgName);
            startActivity(intent);
            Log.d("AAAAAA","成功恢复默认短信程序");
            Toast.makeText(EditSms.this,"成功恢复默认短信程序",Toast.LENGTH_SHORT).show();
        }
    }

}
