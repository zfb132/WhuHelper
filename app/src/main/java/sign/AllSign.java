package sign;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whuzfb.whuhelper.MainActivity;
import com.whuzfb.whuhelper.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zfb15 on 2017/10/8.
 */

public class AllSign extends Activity {
    //控件
    private TextInputEditText et_url=null;
    private TextInputEditText et_param=null;
    private Button btn_up=null;

    private Map<String, String> headers=new HashMap<>();
    public static String url="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allsign);
        initView();
        setListener();
        //不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        url=WebURL.APIHOST_NETEASEMUSIC;
    }

    private void initView(){
        et_url=(TextInputEditText)findViewById(R.id.et_url);
        et_param=(TextInputEditText)findViewById(R.id.et_param);
        btn_up=(Button)findViewById(R.id.btn_up);
    }

    private void setListener(){
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(runPost()).start();
            }
        });
    }

    //设置URL参数
    public Map<String,String> setParams(){
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> post_params=new HashMap<>();
        post_params.put("phone","13297987851");
        post_params.put("password", "1529017133");
        return post_params;
    }

    public Runnable runPost(){
        Runnable rb = new Runnable(){
            @Override
            public void run() {
                Connection.Response res=null;
                try {
                    url=url+WebURL.LOGINURL_NETEASEMUSIC;
                    Connection con= Jsoup.connect(url);
                    //con.header();
                    headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                    con.headers(headers);
                    //设置cookie和post上面的map数据
                    res=con.method(Connection.Method.POST).data(setParams()).execute();
                    showError(res.body());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        return rb;
    }

    //Toast显示字符串
    public void showError(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }
}
