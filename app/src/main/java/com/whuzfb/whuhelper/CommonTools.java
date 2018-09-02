package com.whuzfb.whuhelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sms.EditSms;

public class CommonTools extends Fragment {
    private Context context=null;
    private FragmentActivity fragmentActivity=null;

    private CardView cv_sms=null;

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
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //不自动弹出软键盘
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //初始化各View组件
        initView(view);
        // 设置点击事件监听器
        setListener();
    }

    public void initView(View v){
        cv_sms=(CardView)v.findViewById(R.id.cv_sms);
    }

    public void setListener(){
        cv_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(fragmentActivity, EditSms.class);
                startActivity(intent);
            }
        });
    }
}
