package com.whuzfb.whuhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import sign.AllSign;

/**
 * Created by zfb15 on 2017/5/9.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer=null;
    public static NavigationView navigationView=null;
    private TeachLogin teachLogin=null;
    private AllSign allSign=null;
    private ReserveSeat reserveSeat=null;
    private CommonTools commonTools=null;
    private AboutWhuHelper aboutWhuHelper=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // 图片显示原本的颜色
        // navigationView.setItemIconTintList(null);
    }


    @Override
    public void onBackPressed() {
        if(drawer != null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 创建选项菜单
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, getSupportFragmentManager().getFragments().toString(), Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment currFragment=null;
        String tag = "";
        if (id == R.id.nav_teach) {
            if(teachLogin==null){
                teachLogin=new TeachLogin();
            }
            currFragment=teachLogin;
            // 可能之后会手动设置tag
            tag=getString(R.string.menu_name_teach);
        } else if (id == R.id.nav_seat) {
            if(reserveSeat==null){
                reserveSeat=new ReserveSeat();
            }
            currFragment=reserveSeat;
            // 可能之后会手动设置tag
            tag=getString(R.string.menu_name_seat);
        } else if (id == R.id.nav_allsign) {
            if(allSign==null){
                allSign=new AllSign();
            }
            currFragment=allSign;
            // 可能之后会手动设置tag
            tag=getString(R.string.menu_name_allsign);
        } else if (id == R.id.nav_tools) {
            if(commonTools==null){
                commonTools=new CommonTools();
            }
            currFragment=commonTools;
            // 可能之后会手动设置tag
            tag=getString(R.string.menu_name_tools);
        } else if (id == R.id.nav_about) {
            if(aboutWhuHelper==null){
                aboutWhuHelper=new AboutWhuHelper();
            }
            currFragment=aboutWhuHelper;
            // 可能之后会手动设置tag
            tag=getString(R.string.menu_name_about);
        }
        // 只有当前Fragment不为空才操作
        if(currFragment != null){
            // ActionBar设置标题
            getSupportActionBar().setTitle(tag);
            // 防止重复添加而崩溃
            if(!currFragment.isAdded()){
                //第一个参数指定Fragment的容器的Id
                ft.add(R.id.fg,currFragment,tag);
                // 替换某个id对应的fragment(会重新创建一次fragment)
                //ft.replace(R.id.main_layout, currfg);
            }
            hideFragments(ft);
            ft.show(currFragment);
            ft.commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
        // 设置菜单条目为被点击状态
        //item.setCheckable(true);
        // 关闭抽屉
        //mDrawerMain.closeDrawer(GravityCompat.START);
        // 默认返回false, 如返回true, 更新UI为被点击状态
        //return true;
    }

    // 隐藏所有的Fragment
    public void hideFragments(FragmentTransaction ft){
        if(teachLogin != null){
            ft.hide(teachLogin);
        }
        if(allSign != null){
            ft.hide(allSign);
        }
        if(reserveSeat != null){
            ft.hide(reserveSeat);
        }
        if(commonTools != null){
            ft.hide(commonTools);
        }
        if(aboutWhuHelper != null){
            ft.hide(aboutWhuHelper);
        }
    }
}