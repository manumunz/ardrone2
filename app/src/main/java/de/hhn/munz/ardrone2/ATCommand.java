package de.hhn.munz.ardrone2;

class ATCommand {
    static String keepAlive() {
        return "AT*COMWDG=1\r";
    }

    static String land() {
        return "AT*REF=%d,290717696\r";
    }

    static String takeOff() {
        return "AT*REF=%d,290718208\r";
    }

    static String move(float pitch, float roll, float gaz, float yaw) {
        return "AT*PCMD=%d,1,"
                + Float.floatToIntBits(pitch) + ","
                + Float.floatToIntBits(roll) + ","
                + Float.floatToIntBits(gaz) + ","
                + Float.floatToIntBits(yaw) + "\r";
    }

    static String trim() {
        return "AT*FTRIM=%d\r";
    }
}
