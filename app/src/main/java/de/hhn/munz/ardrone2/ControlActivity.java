package de.hhn.munz.ardrone2;

import android.graphics.PixelFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;

import de.hhn.munz.ardrone2.util.JoystickView;
import de.hhn.munz.ardrone2.util.OnJoystickMovedListener;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;

public class ControlActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    private static final String TAG = ".ControlActivity";

    private static final float SPEED_MIN = 0.2f; // 0.0 to 1.0
    private static final float SPEED_MAX = 1.0f;

    private float speed;

    WifiManager wifiManager;

    JoystickView leftJoystick;
    JoystickView rightJoystick;

    JoystickListener leftJoystickListener;
    JoystickListener rightJoystickListener;

    Switch speedSwitch;

    NetworkController controller;

    boolean isRunning;
    boolean isFlying;
    boolean wifiActive;
    boolean checkWifi;

    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        leftJoystickListener = new JoystickListener();
        rightJoystickListener = new JoystickListener();

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        leftJoystick.setOnJostickMovedListener(leftJoystickListener);

        rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
        rightJoystick.setOnJostickMovedListener(rightJoystickListener);

        speed = SPEED_MIN;
        speedSwitch = (Switch) findViewById(R.id.switch1);
        speedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                speed = isChecked ? SPEED_MAX : SPEED_MIN;
            }
        });

        controller = NetworkController.getInstance();
        isFlying = false;
        isRunning = true;
        wifiActive = true;
        checkWifi = true;

        sendCommand("AT*CONFIG=%d,\"video:video_codec\",\"128\"\r");

        LibsChecker.checkVitamioLibs(this);

        mPreview = (SurfaceView) findViewById(R.id.videoView);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);

        checkWiFi();
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
                        float pitch = (float) leftJoystickListener.getLastX() / 10f * speed;
                        float roll = (float) leftJoystickListener.getLastY() / -10f * speed;
                        float gaz = (float) rightJoystickListener.getLastY() / 10f * speed;
                        float yaw = (float) rightJoystickListener.getLastX() / 10f * speed;

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
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }

    public void onClickLanding(View view) {
        if (isFlying) {
            sendCommand(ATCommand.land());
            findViewById(R.id.btnLanding).setBackground(ContextCompat.getDrawable(this, R.drawable.takeoff));
        } else {
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


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mMediaPlayer = new MediaPlayer(this);
        try {
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.setDataSource("tcp://192.168.1.1:5555/");
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // ignored
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ignored
    }

    public class JoystickListener implements OnJoystickMovedListener {
        private int lastX = 0;
        private int lastY = 0;

        int getLastX() {
            if (lastX < -10 || lastX > 10)
                return lastX = 0;
            return lastX;
        }

        int getLastY() {
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
