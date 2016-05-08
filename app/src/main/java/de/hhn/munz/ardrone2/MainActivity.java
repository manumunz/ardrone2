package de.hhn.munz.ardrone2;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = ".MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // connect button callback
    public void onWiFiConnect(View view) {
        String ssid = ((EditText)findViewById(R.id.wifi_ssid)).getText().toString();
        String key = ((EditText)findViewById(R.id.wifi_key)).getText().toString();

        if (!ssid.isEmpty() && !key.isEmpty()) {
            wifiConnect(ssid,key);
        }
    }

    private void wifiConnect(String ssid, String presharedKey) {
        // WiFi setup with credentials
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", presharedKey);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        // check WiFi enabled, if not enable it
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // Add ArDrone's WiFi to phone and connect
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        boolean success = wifiManager.reconnect();

        if (!success) {
            Toast.makeText(this,"Connection failed.",Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Connection failed!");
        } else {
            Log.w(TAG, "Connection succeeded!");
            // Switch to Control Intent
        }
    }
}
