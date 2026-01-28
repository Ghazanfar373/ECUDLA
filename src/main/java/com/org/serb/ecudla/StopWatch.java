package com.org.serb.ecudla;

public class StopWatch {
    private static final long startTime = System.nanoTime();
    private static boolean running = false;
     static String time="00:00:00";
    public static String displayElapsedTime(long currentTime) {

        if (Params.getInstance().getRPM() > 100 ) {

            //running = true;
            long elapsedTime = currentTime - startTime;
            long seconds = elapsedTime / 1_000_000_000L;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            seconds %= 60;
            minutes %= 60;

            //System.out.printf("Application running time: %02d:%02d:%02d%n", hours, minutes, seconds);
            time = String.format("%02d:%02d:%02d%n", hours, minutes, seconds);
            //return String.format("%02d:%02d:%02d%n", hours, minutes, seconds);
        }


      return time;

    }




}
