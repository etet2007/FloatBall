package com.wangxiandeng.floatball;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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

    private Button mBtnStart;
    private Button mBtnQuit;
    private DiscreteSeekBar opacitySeekbar;
    private MaterialAnimatedSwitch swith;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        swith = (MaterialAnimatedSwitch) findViewById(R.id.start_switch);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnQuit = (Button) findViewById(R.id.btn_quit);
        swith.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b) {
                if(b){
                    checkAccessibility();
                    Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                    Bundle data = new Bundle();
                    data.putInt("type", FloatBallService.TYPE_ADD);
                    intent.putExtras(data);
                    startService(intent);
                    opacitySeekbar.setEnabled(true);
                }else{
                    Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                    Bundle data = new Bundle();
                    data.putInt("type", FloatBallService.TYPE_DEL);
                    intent.putExtras(data);
                    startService(intent);
                    opacitySeekbar.setEnabled(false);
                }
            }
        });
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                设置辅助功能
                checkAccessibility();
                Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                Bundle data = new Bundle();
                data.putInt("type", FloatBallService.TYPE_ADD);
                intent.putExtras(data);
                startService(intent);
                opacitySeekbar.setEnabled(true);
            }
        });
        mBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                Bundle data = new Bundle();
                data.putInt("type", FloatBallService.TYPE_DEL);
                intent.putExtras(data);
                startService(intent);
                opacitySeekbar.setEnabled(false);
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

    private void checkAccessibility() {
        // 判断辅助功能是否开启，看不懂
        if (!AccessibilityUtil.isAccessibilitySettingsOn(this)) {
            // 引导至辅助功能设置页面
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "请先开启FloatBall辅助功能", Toast.LENGTH_SHORT).show();
        }
    }
}
