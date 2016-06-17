package de.hhn.munz.ardrone2;

public class ATCommand {
    public static String keepAlive() {
        return "AT*COMWDG=1\r";
    }

    public static String hover() {
        return "AT*PCMD=%d,0,0,0,0,0\r";
    }

    public static String land() {
        return "AT*REF=%d,290717696\r";
    }

    public static String takeOff() {
        return "AT*REF=%d,290718208\r";
    }

    public static String rotate() {return "AT*PCMD=%d,1,0,0,0,1061158912\r";}

    public static String move(float pitch, float roll, float gaz, float yaw) {
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

    public static String trim() {
        return "AT*FTRIM=%d\r";
    }
}
