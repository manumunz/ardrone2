package de.hhn.munz.ardrone2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = ".MainActivity";
    private boolean isRunning;
    private boolean isProcessing;
    private WifiManager wifiManager;

    private ProgressDialog mainProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        // check WiFi enabled, if not enable it
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getSSID().toLowerCase().contains("ardrone")) {
            Toast.makeText(this,"Connected to ArDrone WiFi",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ControlActivity.class);
            this.startActivity(intent);
            return;
        }

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                processNetworks(wifiManager.getScanResults());
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        isRunning = true;
        isProcessing = false;
        runNetworkScan();

        mainProgress = new ProgressDialog(this);
        mainProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mainProgress.setMessage("Searching ArDrone WiFi..");
        mainProgress.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mainProgress.setCancelable(true);

        mainProgress.show();
    }

    private void processNetworks(List<ScanResult> scanResults) {
        for (ScanResult scan : scanResults) {
            if (scan.SSID.toLowerCase().contains("ardrone")) {
                isRunning = false;
                wifiConnect(scan.SSID);
                break;
            }
        }
        isProcessing = false;
    }

    private void runNetworkScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        if (!isProcessing) {
                            isProcessing = wifiManager.startScan();
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage());
                    }
                }
                mainProgress.dismiss();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    private void wifiConnect(String ssid) {
        // WiFi setup with credentials
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // Add ArDrone's WiFi to phone and connect
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        boolean success = wifiManager.reconnect();

        if (!success) {
            Toast.makeText(this,"Connection failed.",Toast.LENGTH_LONG).show();
            Log.w(TAG, "Connection failed!");
        } else {
            Log.w(TAG, "Connection succeeded!");

            Intent intent = new Intent(this, ControlActivity.class);
            this.startActivity(intent);
        }
    }
}
