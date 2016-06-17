package de.hhn.munz.ardrone2;

import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import de.hhn.munz.ardrone2.util.JoystickView;
import de.hhn.munz.ardrone2.util.OnJoystickMovedListener;

public class ControlActivity extends AppCompatActivity {
    private static final String TAG = ".ControlActivity";

    private static final float SPEED_FACTOR = 0.2f; // 0.0 to 1.0

    WifiManager wifiManager;

    JoystickView leftJoystick;
    JoystickView rightJoystick;

    JoystickListener leftJoystickListener;
    JoystickListener rightJoystickListener;

    NetworkController controller;

    boolean isRunning;
    boolean isFlying;
    boolean wifiActive;
    boolean checkWifi;

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        leftJoystickListener = new JoystickListener();
        rightJoystickListener = new JoystickListener();

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        leftJoystick.setOnJostickMovedListener(leftJoystickListener);

        rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
        rightJoystick.setOnJostickMovedListener(rightJoystickListener);

        controller = NetworkController.getInstance();
        isFlying = false;
        isRunning = true;
        wifiActive = true;
        checkWifi = true;

        sendCommand("AT*CONFIG=%d,\"video:video_codec\",\"128\"\r");

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.requestFocus();
        videoView.setMediaController(new MediaController(this));

        try {
            videoView.setVideoURI(Uri.parse("http://192.168.1.1:5555"));
        }
        catch (Exception e) {}

        getVideoStream();
        checkWiFi();
    }

    private void getVideoStream() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        if (!videoView.isPlaying()) {
                            videoView.start();
                        }
                    } catch (Exception e) {
                        try {
                            videoView.setVideoURI(Uri.parse("http://192.168.1.1:5555"));
                        }
                        catch (Exception e1) {}
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {}
                }
            }}).start();
    }

    private void checkWiFi() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (checkWifi) {
                    try {
                        WifiInfo info = wifiManager.getConnectionInfo();
                        if (info != null && info.getSSID().toLowerCase().contains("ardrone")) {
                            if (!wifiActive) {
                                findViewById(R.id.led).setBackground(ContextCompat.getDrawable(ControlActivity.this, R.drawable.green_led));
                                wifiActive = true;
                            }
                        } else {
                            findViewById(R.id.led).setBackground(ContextCompat.getDrawable(ControlActivity.this, R.drawable.red_led));
                            wifiActive = false;
                        }

                        Thread.sleep(3000);
                    } catch (Exception e) {
                        Log.w(TAG, "CheckWiFi: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void processMovement() {
        isRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    try {
                        float pitch = (float) leftJoystickListener.getLastX() / 10f * SPEED_FACTOR;
                        float roll = (float) leftJoystickListener.getLastY() / -10f * SPEED_FACTOR;
                        float gaz = (float) rightJoystickListener.getLastY() / 10f * SPEED_FACTOR;
                        float yaw = (float) rightJoystickListener.getLastX() / -10f * SPEED_FACTOR;

                        roll = roll == -0.0f ? 0.0f : roll;
                        yaw = yaw == -0.0f ? 0.0f : yaw;

                        controller.sendString(ATCommand.move(pitch, roll, gaz, yaw));

                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage());
                    }

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.getMessage());
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        checkWifi = false;
    }

    public void onClickLanding(View view) {
        if (isFlying) {
            sendCommand(ATCommand.land());
            findViewById(R.id.btnLanding).setBackground(ContextCompat.getDrawable(this, R.drawable.takeoff));
        }
        else {
            sendCommand(ATCommand.takeOff());
            findViewById(R.id.btnLanding).setBackground(ContextCompat.getDrawable(this, R.drawable.land));
        }
        isFlying = !isFlying;
    }

    public void onClickTrim(View view) {
        isRunning = false;
        sendCommand(ATCommand.trim());
    }

    private void sendCommand(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    controller.sendString(cmd);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        }).start();
    }

    public class JoystickListener implements OnJoystickMovedListener {
        private int lastX = 0;
        private int lastY = 0;

        public int getLastX() {
            if (lastX < -10 || lastX > 10)
                return lastX = 0;
            return lastX;
        }

        public int getLastY() {
            if (lastY < -10 || lastY > 10)
                return lastY = 0;
            return lastY;
        }

        @Override
        public void onMoved(int x, int y) {
            lastX = x;
            lastY = y;
            processMovement();
        }
    }
}
