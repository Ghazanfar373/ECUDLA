package com.org.serb.ecudla;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataLogger {
    private FileWriter realTimeWriter;// =new FileWriter("DataLog-"+formattedDate+".txt");
    private boolean mRunning = false;
    private boolean realRunning=false;
    private Date date;
    public DataLogger() {
        //init stuff
        try {
            date = new Date();
            Timestamp ts = new Timestamp(date.getTime());
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HHmm").format(ts);
            File folder = logFolder();
            System.out.println("DataLogging Started... ");
            System.out.println(folder.getAbsolutePath());
            realTimeWriter = new FileWriter(new File(folder, "RealTime-" + formattedDate + ".csv"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //
    //......................................Your Portion
    //Display your name here
    //..............................................
    public synchronized void onEvent() {
        //...
        try {
            if (realRunning) {
                // Date date=new Date();
                //Instant instant = Instant.now();
                //SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");
                //Timestamp ts=new Timestamp(date.getTime());
                Calendar now = Calendar.getInstance();
                String ts = now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE)+":"+now.get(Calendar.SECOND);
                String dataLog =ts+","+ Params.getInstance().getRPM() + ","+Params.getInstance().getTPS()+ "," +Params.getInstance().getAdvanceAngle() + ","+Params.getInstance().getInjectorPulse()+ ","  + Params.getInstance().getTempClndr1() + "," + Params.getInstance().getTempClndr2() + "," +Params.getInstance().getIAT()+","+ Params.getInstance().getFuelConsumed() + "," + Params.getInstance().getFuelperHour()+ ","+Params.getInstance().getVol1()+  "\r\n";
                     //   + "," + Params.getInstance().getBARO() + "," + Params.getInstance().getFuelConsumption() + "," + Params.getInstance().getFuelLevelSML02() + "," + Params.getInstance().getFuelinLitersSML02()
                     //   + "," + Params.getInstance().getVol1() + "," + Params.getInstance().getVol2() + "," + Params.getInstance().getVol3() + "," + Params.getInstance().getVol4() + "," + Params.getInstance().getVol5() + "," + Params.getInstance().getDownlinkBuffer() + "\r\n";
                realTimeWriter.write(dataLog);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public synchronized void start() {
        //startdata logging
        realRunning = true;
        initialize();
    }
public synchronized void stopRealTime(){
        realRunning=false;
        try {
            realTimeWriter.close();
        }catch (IOException x){
            x.printStackTrace();
        }
}
    public synchronized void stop()  {
        //stop data logging
        try {
            realRunning = false;
            //closing File writer etc.
            realTimeWriter.close();
           // myWriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
   //Let me show you another criteria of being held by data centers
       public  synchronized  void initialize(){
        try {
            String data = "Timestamp" + "," +"RPM" + "," + "Throttle" + "," + "Advanced Angle" + "," + "InjectorPulse" + "," + "Temp Cylinder 1" + ","+"Temp Cylinder 2" + "," +"BARO"+ ","+ "fuel Consumed L/H" + "," + "fuel L/H"+ "," + "ECU Volt" + "\r\n";//,"+"FuelCons"+","+"FuelLvlT1"+","+"FuelLiters"+","+"VoltSrc1"+","+"VoltSrc2"+","+"VoltSrc3"+","+"VoltSrc4"+","+"VoltSrc5"+" \r\n";
            realTimeWriter.write(data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public  synchronized  void logRealTime(double winspeed,int angle){
        try {
            if(realRunning) {
                Date date=new Date();
                Timestamp ts=new Timestamp(date.getTime());
                String formattedDate = new SimpleDateFormat("HHmmss").format(ts);
                String data = formattedDate+","+winspeed + "," + angle + "\r\n";
                realTimeWriter.write(data);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static File logFolder() {
        File dir = new File(System.getProperty("user.home") + "\\Documents\\SERB_ECU_LOGS");
        dir.mkdirs();
        return dir;
    }
//   public void updateLogger(int payloadNumber) {
//        try {
//
//           // Timestamp ts=new Timestamp(date.getTime());
//           // String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HHmm").format(ts);
//         //   System.out.println(formattedDate);
//            // myWriter = new FileWriter("DataLogAllinOne-"+formattedDate+".txt");
//            String data=payloadNumber+","+BtnHexData.getInstance().getWindSpeed()+","+BtnHexData.getInstance().getWindAngle()+","+BtnHexData.getInstance().getLatitude()+","+BtnHexData.getInstance().getLongitude();
//            myWriter.write(data);
//            myWriter.close();
//            System.out.println("Successfully wrote to the file.");
//        } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//    }
}
