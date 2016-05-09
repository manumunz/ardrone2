package de.hhn.munz.ardrone2;

public class ATCommand {
    public static String keepAlive() {
        return "AT*COMWDG=%d\r";
    }

    public static String hover() {
        return "AT*PCMD=%d,1,0,0,0,0\r";
    }

    public static String land() {
        return "AT*REF=%d,290717696\r";
    }

    public static String takeOff() {
        return "AT*REF=%d,290718208\r";
    }

    public static String move(float pitch, float roll, float gaz, float yaw) {
        pitch = pitch == -0.0f ? 0.0f : pitch;
        roll = roll == -0.0f ? 0.0f : roll;
        gaz = gaz == -0.0f ? 0.0f : gaz;
        yaw = yaw == -0.0f ? 0.0f : yaw;

        return "AT*PCMD=%d,1,"
                + Float.floatToIntBits(pitch) + ","
                + Float.floatToIntBits(roll) + ","
                + Float.floatToIntBits(gaz) + ","
                + Float.floatToIntBits(yaw) + "\r";
    }

    public static String forward(float speed) {
        return "AT*PCMD=%d,1,0," + Float.floatToIntBits(-speed) + ",0,0\r";
    }

    public static String backward(float speed) {
        return "AT*PCMD=%d,1,0," + Float.floatToIntBits(speed) + ",0,0\r";
    }

    public static String left(float speed) {
        return "AT*PCMD=%d,1," + Float.floatToIntBits(-speed) + ",0,0,0\r";
    }

    public static String right(float speed) {
        return "AT*PCMD=%d,1," + Float.floatToIntBits(speed) + ",0,0,0\r";
    }

    public static String up(float speed) {
        return "AT*PCMD=%d,1,0,0," + Float.floatToIntBits(speed) + ",0\r";
    }

    public static String down(float speed) {
        return "AT*PCMD=%d,1,0,0," + Float.floatToIntBits(-speed) + ",0\r";
    }

    public static String rotateLeft(float speed) {
        return "AT*PCMD=%d,1,0,0,0," + Float.floatToIntBits(-speed) + "\r";
    }

    public static String rotateRight(float speed) {
        return "AT*PCMD=%d,1,0,0,0," + Float.floatToIntBits(speed) + "\r";
    }

    public static String maxAltitude(int millimeter) {
        return "AT*CONFIG=%d,\"control:altitude_max\",\""+millimeter+"\"\r";
    }

    public static String emergencyReset() {
        return "AT*REF=%d,290717952\r";
    }
}
