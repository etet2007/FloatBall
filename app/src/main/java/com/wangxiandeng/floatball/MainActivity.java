package com.wangxiandeng.floatball;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/*

判断版本，使用Build.VERSION.SDK_INT

权限设置 ACTION_MANAGE_OVERLAY_PERMISSION
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

辅助功能

*/
public class MainActivity extends Activity {

    //控件
    private ImageView logoImageView;
    private SwitchCompat ballSwitch;
    private DiscreteSeekBar opacitySeekBar;
    private DiscreteSeekBar sizeSeekBar;
    private Button choosePicButton;

    //显示参数
    private int opacity;
    private int ballSize;
    private boolean isOpenBall;

    SharedPreferences prefs;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;

    private final int mREQUEST_external_storage = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取悬浮球参数，用于初始化悬浮球
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isOpenBall=prefs.getBoolean("isOpenBall",false);
        opacity=prefs.getInt("opacity",125);
        ballSize=prefs.getInt("opacity",25);

        initView();
        //判断版本，使用Build.VERSION.SDK_INT
        //Build: Information about the current build, extracted from system properties.
        //Build.VERSION: The user-visible SDK version of the framework; its possible values are defined in Build.VERSION_CODES.
        //23 Marshmallow
        if (Build.VERSION.SDK_INT >= 23) {
            //Setting :The Settings provider contains global system-level device preferences.
            //Checks if the specified context can draw on top of other apps. As of API level 23,
            // an app cannot draw on top of other apps unless it declares the SYSTEM_ALERT_WINDOW permission
            // in its manifest, and the user specifically grants the app this capability.
            // To prompt the user to grant this approval, the app must send an intent with the action
            // ACTION_MANAGE_OVERLAY_PERMISSION, which causes the system to display a permission management screen.
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
                Toast.makeText(this, "请先允许FloatBall出现在顶部", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView() {
        opacitySeekBar = (DiscreteSeekBar) findViewById(R.id.opacity_seekbar);
        logoImageView = (ImageView) findViewById(R.id.logo_imageview);
        ballSwitch = (SwitchCompat) findViewById(R.id.start_switch);
        sizeSeekBar = (DiscreteSeekBar) findViewById(R.id.size_seekbar);
        choosePicButton = (Button) findViewById(R.id.choosePic_button);

        choosePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查权限 请求权限 选图片
//                android 6.0以上才需要?
                requestStoragePermission();

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });
        opacitySeekBar.setProgress(opacity);

        sizeSeekBar.setProgress(ballSize);

        if(isOpenBall) {
            ballSwitch.setChecked(true);
            logoImageView.getBackground().setAlpha(255);
            opacitySeekBar.setEnabled(true);
            sizeSeekBar.setEnabled(true);
            //这样才会调用Listener接口
//            ballSwitch.post(new Runnable() {
//                public void run() {
//                    ballSwitch.toggle();
//                }
//            });
        }else{
            logoImageView.getBackground().setAlpha(125);
            ballSwitch.setChecked(false);
            opacitySeekBar.setEnabled(false);
            sizeSeekBar.setEnabled(false);

        }

        logoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ballSwitch.toggle();
            }
        });
        ballSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lqt", "onCheckedChanged: ");
                if(isChecked){
                    openFloatBall();
                }else{
                    removeFloatBall();
                }
            }
        });

        opacitySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                Bundle data = new Bundle();
                data.putInt("type", FloatBallService.TYPE_OPACITY);
                data.putInt("opacity", value);
                intent.putExtras(data);
                startService(intent);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                opacity = seekBar.getProgress();
            }
        });
        sizeSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                Bundle data = new Bundle();
                data.putInt("type", FloatBallService.TYPE_SIZE);
                data.putInt("size", value);
                intent.putExtras(data);
                startService(intent);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                ballSize = seekBar.getProgress();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);

            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            Log.d("lqt", "onActivityResult: "+imagePath);
            Intent intent = new Intent(MainActivity.this, FloatBallService.class);
            Bundle bundle = new Bundle();
            bundle.putInt("type", FloatBallService.TYPE_IMAGE);

            bundle.putString("imagePath", imagePath);
            intent.putExtras(bundle);
            startService(intent);

            c.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==mREQUEST_external_storage){
//判断是否成功
//            成功继续打开图片？

        }
    }


    private void requestStoragePermission() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    mREQUEST_external_storage
            );
        }
    }

    private void removeFloatBall() {
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_DEL);
        intent.putExtras(data);
        startService(intent);

        logoImageView.getBackground().setAlpha(125);
        opacitySeekBar.setEnabled(false);
        sizeSeekBar.setEnabled(false);
        isOpenBall=false;
    }

    private void openFloatBall() {
        checkAccessibility();
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_ADD);
        intent.putExtras(data);
        startService(intent);

        logoImageView.getBackground().setAlpha(255);
        opacitySeekBar.setEnabled(true);
        sizeSeekBar.setEnabled(true);
        isOpenBall=true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isOpenBall", isOpenBall); // value to store
        editor.putInt("opacity",opacity);
        editor.putInt("size",ballSize);

        editor.apply();

    }

    private void checkAccessibility() {
        // 判断辅助功能是否开启，看不懂
        if (!AccessibilityUtil.isAccessibilitySettingsOn(this)) {
            // 引导至辅助功能设置页面
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "请先开启FloatBall辅助功能", Toast.LENGTH_SHORT).show();
        }
    }
}
