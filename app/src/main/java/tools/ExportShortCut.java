package tools;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.whuzfb.whuhelper.R;

public class ExportShortCut extends AppCompatActivity {
    private Button btn_create=null;
    private EditText et_pkgname=null;
    private EditText et_action=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exportsc);
        // findview
        btn_create=(Button)findViewById(R.id.btn_create);
        et_pkgname=(EditText)findViewById(R.id.edt_pkgname);
        et_action=(EditText)findViewById(R.id.edt_actionname);

        // 设置默认文本
        et_pkgname.setText("com.miui.cit");
        et_action.setText("com.xiaomi.cameratest.MainActivity");

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClassName(et_pkgname.getText().toString(),et_action.getText().toString());
                intent.setAction(et_action.getText().toString());
                addShortcut("红外相机",intent);
                //startActivity(mIntent);
                Toast.makeText(ExportShortCut.this,"创建快捷方式成功",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addShortcut(String name, Intent actionIntent) {
        if (Build.VERSION.SDK_INT < 28) {
            //  创建快捷方式的intent广播
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 添加快捷名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            //  快捷图标是允许重复(不一定有效)
            shortcut.putExtra("duplicate", false);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, Intent.ShortcutIconResource.fromContext(this, R.mipmap.lena));
            // 添加携带的下次启动要用的Intent信息
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
            // 发送广播
            this.sendBroadcast(shortcut);
        } else {
            ShortcutManager shortcutManager = (ShortcutManager)this.getSystemService(Context.SHORTCUT_SERVICE);
            if (null == shortcutManager) {
                // 创建快捷方式失败
                Log.d("ExportShortCut", "创建快捷方式失败");
                return;
            }
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(this, name)
                    .setShortLabel(name)
                    .setIcon(Icon.createWithResource(this, R.mipmap.lena))
                    .setIntent(actionIntent)
                    .setLongLabel(name)
                    .build();
            shortcutManager.requestPinShortcut(
                    shortcutInfo,
                    PendingIntent.getActivity(this, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender()
            );
        }
    }

}
