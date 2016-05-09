package de.hhn.munz.ardrone2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import de.hhn.munz.ardrone2.util.JoystickView;
import de.hhn.munz.ardrone2.util.OnJoystickMovedListener;

public class ControlActivity extends AppCompatActivity {
    private static final String TAG = ".ControlActivity";

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

    private void networkLoop() {
        isRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(25);

                        float pitch = (float) leftJoystickListener.getLastX() / 10f;
                        float roll = (float) leftJoystickListener.getLastY() / -10f;
                        float gaz = (float) rightJoystickListener.getLastY() / 10f;
                        float yaw = (float) rightJoystickListener.getLastX() / -10f;

                        controller.sendString(ATCommand.move(pitch, roll, gaz, yaw));
                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage());
                    }
                }
            }
        }).start();
    }

    public void disableLoop() {
        isRunning = false;
    }

    public void onClickLanding(View view) {
        sendCommand(ATCommand.land());
    }

    public void onClickTakeOff(View view) {
        sendCommand(ATCommand.takeOff());
        networkLoop();
    }

    public void onClickEmergency(View view) {
        disableLoop();
        sendCommand(ATCommand.emergencyReset());
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
        }
    }
}
