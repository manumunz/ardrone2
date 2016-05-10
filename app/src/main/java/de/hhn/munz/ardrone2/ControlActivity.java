package de.hhn.munz.ardrone2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import de.hhn.munz.ardrone2.util.JoystickView;
import de.hhn.munz.ardrone2.util.OnJoystickMovedListener;

public class ControlActivity extends AppCompatActivity {
    private static final String TAG = ".ControlActivity";

    private static final float SPEED_FACTOR = 0.2f; // 0.0 to 1.0

    JoystickView leftJoystick;
    JoystickView rightJoystick;

    JoystickListener leftJoystickListener;
    JoystickListener rightJoystickListener;

    NetworkController controller;

    boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        leftJoystickListener = new JoystickListener();
        rightJoystickListener = new JoystickListener();

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        leftJoystick.setOnJostickMovedListener(leftJoystickListener);

        rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
        rightJoystick.setOnJostickMovedListener(rightJoystickListener);

        controller = NetworkController.getInstance();
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

                        controller.sendString(ATCommand.move(pitch, roll, gaz, yaw), true);

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
    }

    public void onClickLanding(View view) {
        sendCommand(ATCommand.land(), false);
    }

    public void onClickTakeOff(View view) {
        sendCommand(ATCommand.takeOff(), false);
    }

    public void onClickTrim(View view) {
        isRunning = false;
        sendCommand(ATCommand.trim(), false);
    }

    private void sendCommand(final String cmd, final boolean withRetries) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    controller.sendString(cmd, withRetries);
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
