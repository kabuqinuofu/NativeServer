package com.yc.nativeserver;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yc.httpserver.NativeServerManager;
import com.yc.httpserver.listener.UploadListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements UploadListener {

    NativeServerManager mNativeServerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView notice_tv = findViewById(R.id.notice_tv);
        mNativeServerManager = NativeServerManager.init(this);
        mNativeServerManager.setUploadListener(this);
        mNativeServerManager.setResDir("wifi");
        mNativeServerManager.setPort(12345);
        notice_tv.setText("内网打开：http://" + getIPAddress() + ":" + mNativeServerManager.getPort());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNativeServerManager != null) {
            mNativeServerManager.startServer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNativeServerManager != null) {
            mNativeServerManager.stopServer();
        }
    }

    @Override
    public void upLoadSuccess(File file) {
        showToast(file.getAbsolutePath());
    }

    @Override
    public void upLoadFailure(Exception e) {
        showToast("传书失败:" + e.toString());
    }

    private void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getIPAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return Formatter.formatIpAddress(wifiInfo.getIpAddress());
    }

}