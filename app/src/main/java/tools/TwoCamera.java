package tools;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.whuzfb.whuhelper.R;

import java.io.IOException;
import java.lang.reflect.Method;

public class TwoCamera extends AppCompatActivity {
    private Camera camera_back = null;
    private Camera camera_front = null;
    private SurfaceView surfaceView_back, surfaceView_front;
    private SurfaceHolder surfaceHolder_back, surfaceHolder_front;
    private Camera.Parameters parameters;
    private String TAG = "TwoCamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twocamera);
        init();
        // 后置摄像头配置回调
        surfaceHolder_back = surfaceView_back.getHolder();
        surfaceHolder_back.addCallback(new SurfaceHolderCallbackBack());
        // 前置摄像头配置回调
        surfaceHolder_front = surfaceView_front.getHolder();
        surfaceHolder_front.addCallback(new SurfaceHolderCallbackFront());
    }

    private void init(){
        surfaceView_back = (SurfaceView) findViewById(R.id.surfaceView_back);
        surfaceView_front = (SurfaceView) findViewById(R.id.surfaceView_front);
    }

    private void setDisplay(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }
    }

    // 通过反射来设置预览方向
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e(TAG, "设置预览方向失败");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        try{
            if(camera_back!=null)
                camera_back.startPreview();
            if(camera_front!=null)
                camera_front.startPreview();
        } catch (Exception e) {
            camera_back.stopPreview();
            camera_back.release();
            Log.d(TAG, "重新预览异常，已释放相机"+e.toString());
            e.printStackTrace();
        }
        */
    }

    @Override
    protected void onPause() {
        if(camera_back!=null)
            camera_back.release();
        if(camera_front!=null)
            camera_front.release();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(camera_back!=null)
            camera_back.release();
        if(camera_front!=null)
            camera_front.release();
        super.onDestroy();
    }


    class SurfaceHolderCallbackBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (Camera.getNumberOfCameras() > 0) {
                camera_back = Camera.open(0);
                try {
                    camera_back.setPreviewDisplay(holder);
                    Camera.Parameters parameters = camera_back.getParameters();
                    // 设置方向校整
                    if (TwoCamera.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        parameters.set("orientation", "portrait");
                        camera_back.setDisplayOrientation(90);
                        parameters.setRotation(90);
                    } else {
                        parameters.set("orientation", "landscape");
                        camera_back.setDisplayOrientation(0);
                        parameters.setRotation(0);
                    }
                    camera_back.setParameters(parameters);
                    camera_back.startPreview();
                    Log.d(TAG, "surfaceCreated: 启动后置预览");
                } catch (IOException e) {
                    camera_back.stopPreview();
                    camera_back.release();
                    Log.d(TAG, "surfaceCreated: 后置预览异常，已释放相机"+e.toString());
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera_back.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        parameters = camera_back.getParameters();
                        parameters.setPictureFormat(PixelFormat.JPEG);
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        setDisplay(parameters, camera_back);
                        camera_back.setParameters(parameters);
                        camera_back.startPreview();
                        // 要实现连续自动对焦，必须加上
                        camera_back.cancelAutoFocus();
                        camera.cancelAutoFocus();
                    }
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera_back!=null)
                camera_back.release();
        }

    }

    class SurfaceHolderCallbackFront implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 在两个摄像头的情况下开启前摄
            if (Camera.getNumberOfCameras() == 2) {
                camera_front = Camera.open(1);
            }
            try {
                camera_front.setPreviewDisplay(holder);
                Camera.Parameters parameters = camera_front.getParameters();
                if (TwoCamera.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    camera_front.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    camera_front.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                camera_front.setParameters(parameters);
                camera_front.startPreview();
                Log.d(TAG, "surfaceCreated: 启动前置预览");;
            } catch (IOException e) {
                camera_front.stopPreview();
                camera_front.release();
                Log.d(TAG, "surfaceCreated: 前置预览异常，已释放相机"+e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera_front.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        parameters = camera_front.getParameters();
                        parameters.setPictureFormat(PixelFormat.JPEG);
                        // 不能前后同时设置为连续自动对焦
                        setDisplay(parameters, camera_front);
                        camera_front.setParameters(parameters);
                        camera_front.startPreview();
                        camera_front.cancelAutoFocus();
                        camera.cancelAutoFocus();
                    }
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera_front!=null)
                camera_front.release();
        }

    }

}
