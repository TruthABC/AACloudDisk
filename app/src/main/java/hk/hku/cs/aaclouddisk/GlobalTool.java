package hk.hku.cs.aaclouddisk;

public class GlobalTool {

    public static String secondToMinSecText(int second) {
        String min = "" + second / 60;
        String sec = "" + second % 60;
        if (min.length() == 1)
            min = "0" + min;
        if (sec.length() == 1)
            sec = "0" + sec;
        return min + ":" + sec;
    }

}
