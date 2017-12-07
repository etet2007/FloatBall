package com.wangxiandeng.floatball;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/*

判断版本，使用Build.VERSION.SDK_INT

权限设置 ACTION_MANAGE_OVERLAY_PERMISSION
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

辅助功能

*/
public class MainActivity extends Activity {

    private DiscreteSeekBar opacitySeekbar;
    private MaterialAnimatedSwitch ballSwitch;
    private ImageView logoImageview;
    SharedPreferences prefs;
    private boolean isOpenBall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isOpenBall=prefs.getBoolean("isOpenBall",false);

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
        opacitySeekbar = (DiscreteSeekBar) findViewById(R.id.opacity_seekbar);
        logoImageview = (ImageView) findViewById(R.id.logo_imageview);
        logoImageview.getBackground().setAlpha(125);
        ballSwitch = (MaterialAnimatedSwitch) findViewById(R.id.start_switch);
        if(isOpenBall) {
            //不这样写会崩溃
            ballSwitch.post(new Runnable() {
                public void run() {
                    ballSwitch.toggle();
                }
            });
        }
        logoImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpenBall){
                    removeFloatBall();
                }else{
                    openFloatBall();
                }
                ballSwitch.toggle();
            }
        });
        ballSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean check) {
                if(check){
                    openFloatBall();
                }else{
                    removeFloatBall();
                }
            }
        });

        opacitySeekbar.setEnabled(false);
        opacitySeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
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

            }
        });
    }

    private void removeFloatBall() {
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_DEL);
        intent.putExtras(data);
        startService(intent);
        opacitySeekbar.setEnabled(false);
        logoImageview.getBackground().setAlpha(125);

        isOpenBall=false;
    }

    private void openFloatBall() {
        checkAccessibility();
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_ADD);
        intent.putExtras(data);
        startService(intent);
        opacitySeekbar.setEnabled(true);
        logoImageview.getBackground().setAlpha(255);
        isOpenBall=true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isOpenBall", isOpenBall); // value to store
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
