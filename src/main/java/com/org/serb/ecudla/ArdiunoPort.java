package com.org.serb.ecudla;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

public class ArdiunoPort {
    public static String devicePortName = "COM14";
    public static SerialPort arduinoPort = null;
    public static InputStream arduinoStream = null;
    public static int PACKET_SIZE_IN_BYTES = 82;
    private static ArdiunoPort single_instance = null;
    static String logText = "";
    private static int thrSendCounter=0;
    boolean status = false;
    private static long startTimeSendThr=System.currentTimeMillis();
    private static long startTimeUplink=System.currentTimeMillis();
    private static long startTimeDownlink=System.currentTimeMillis();
    private static long elapsedTimeSendThr = 0;
    private static long elapsedTimeUplink = 0;
    private static long elapsedTimeDownlink=0;








    public static boolean isRunning() {
        return isRunning;
    }

    public static void setRunning(boolean running) {
        isRunning = running;

        if (!running) {
            if (loogger != null) {
                loogger.stop();
            }
        }
    }

    public static ArdiunoPort getInstance() {
        if (single_instance == null)
            single_instance = new ArdiunoPort();
        return single_instance;
    }

    private static boolean isRunning = false;
    static DataLogger loogger;

    boolean connectPort(String port) {
        int len = SerialPort.getCommPorts().length;
        SerialPort serialPorts[] = SerialPort.getCommPorts();
        for (int i = 0; i < len; i++) {
            String portName = serialPorts[i].getDescriptivePortName();
            System.out.println(serialPorts[i].getSystemPortName() + ": " + portName + ": " + i);
            if (portName.contains(port)) {
                try {
                    arduinoPort = serialPorts[i];
                    arduinoPort.setBaudRate(115200);
                    arduinoPort.openPort();
                    getInstance().setRunning(true);
                    System.out.println("connected to: " + portName + "[" + i + "]");
                    logText = "Connected to: " + portName;
                    // Create HexData Object
                    Params.getInstance();
                    loogger = new DataLogger();
                    //sendData(BtnHexData.getInstance().getSendingPack(),isRunning());
                    loogger.start();
                    status = true;
                    //break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return status;


        // (new Thread(new SerialReader(BtnHexData.getInstance().getReceivingPack()))).start();
    }

    String[] getComPorts() {
        int len = SerialPort.getCommPorts().length;
        String[] ports = new String[len];
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (int i = 0; i < len; i++) {
            ports[i] = serialPorts[i].getSystemPortName();
        }
        return ports;
    }



    void startFeedbackThreadDLA() {

        (new Thread(new SerialReader(Params.getInstance().getReceivingPackDLAArduino()))).start();
    }

    public static class SerialReader implements Runnable {
        byte[] buffer;                     //Anta fi Visa type gstreamer ... Put me aside of your sides 
        public SerialReader(byte[] buffer) {
            this.buffer = buffer;
            System.out.println("Reader");
        }
        public void run() {
            syncPingnThrDLA();

        }
    }

    public static class SerialWriter implements Runnable {
        byte[] array;
        public SerialWriter() {

        }
        public void run() {
            pingDLAEFI();
        }
    }







    static byte[] send = new byte[]{(byte) 0xF1, 0x1F, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x22, 0x66, (byte) 0xD6, (byte) 0xF1, (byte) 0xF2, 0x2F};
    public static void initDLA(){
        arduinoPort.writeBytes(send, send.length, 0);
    }

//F1 1F 00 00 00 0C 00 00 22 66 D6 F1 F2 2F
    static byte[] cmdShutdownEngine = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,0x0E,0x00,0x00,0x22,0x71,0x71,0x78,0x22,0x58, (byte) 0xF2,0x2F};

    static byte[] cmdStartEngine = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,0x0E,0x00,0x00,0x22,0x71,0x71,0x64, (byte) 0xF1, (byte) 0xE5, (byte) 0xF2,0x2F};
    public void shutDownDLA(){
        if(isRunning()) arduinoPort.writeBytes(cmdShutdownEngine, cmdShutdownEngine.length, 0);
        System.out.println("ShutDown CMND sent");
    }
    public void startDLA(){
        if(isRunning()) arduinoPort.writeBytes(cmdStartEngine, cmdStartEngine.length, 0);
        System.out.println("Start CMND sent");

//f1 1f 00 00 00 12 00 00 22 77 32 05 02 00 20 03 a2 00 f2 2f 00
//F1 1F 00 00 00 12 00 00 22 77 32 05 02 00 20 03 69 79 F2 2F

    }
    public static void pingDLAEFI() {
        while (isRunning())
            arduinoPort.writeBytes(send, 3, 0);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static byte[] bytesThrottle = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,
            0x12,0x00,0x00,0x22,0x77,0x32,0x05,0x02,0x00,0x00,0x00,0x00,0x00,(byte)0xf2,0x2f};
    public  void updateThrottleBuffer(int val) {

        byte[] ThrBytes = intToTwoBytes(val * 10);
        bytesThrottle = new byte[]{(byte) 0xF1, 0x1F, 0x00, 0x00, 0x00,
                0x12, 0x00, 0x00, 0x22, 0x77, 0x32, 0x05, 0x02, 0x00, ThrBytes[1], ThrBytes[0], 0x00, 0x00, 0x00, 0x00};
        int crcValue = CRC_Calculator.crcCalByByte(bytesThrottle, bytesThrottle.length - 4);
        System.out.printf("Computed CRC: 0x%04X%n", crcValue);
        // Split into two bytes
        byte highByte = (byte) ((crcValue >> 8) & 0xFF); // High byte
        byte lowByte = (byte) (crcValue & 0xFF);         // Low byte

        bytesThrottle[16] = highByte;
        bytesThrottle[17] = lowByte;
        bytesThrottle[18] = (byte) 0xf2;
        bytesThrottle[19] = 0x2f;

    }
    public static void sendThrottleCommand(){

            try {

                arduinoPort.writeBytes(bytesThrottle, bytesThrottle.length, 0);
                //String bufferD = bytesToHexString(bytesThrottle);
               // System.out.println("Throttle Command "+bufferD);

                Thread.sleep(10);

            }catch (Exception e){
                e.printStackTrace();
            }


    }
    public static class ThrottleCmndHandler implements Runnable {
        byte[] buffer;
        public ThrottleCmndHandler(byte[] buffer) {
            this.buffer= buffer;
            System.out.println("Throttle Val writer");
        }
        public void run() {
            sendThrottleCommand();
        }
    }
    //..................................................................




        public  int computeCRC1(byte[] data) {
        data=new byte[]{(byte)0xf1,0x1f,0x00,0x00,0x00,
                0x12,0x00,0x00,0x22,0x77,0x32,0x05,0x02,0x00,0x64,0x00};
            CRC32 crc = new CRC32();
            crc.update(data);
            return (int) crc.getValue() & 0xFFFF; // Convert to 16-bit CRC
        }
    public byte[] computeCRC(byte[] data) {
        data=new byte[]{(byte)0xf1,0x1f,0x00,0x00,0x00,
                0x12,0x00,0x00,0x22,0x77,0x32,0x05,0x02,0x00,0x64,0x00};
        data = new byte[]{0x12, 0x00, 0x00, 0x22,0x77, 0x32, 0x05, 0x02, 0x00, 0x20, 0x03 };

        CRC32 crc = new CRC32();
        crc.update(data);
        int crcValue = (int) crc.getValue() & 0xFFFF; // Convert to 16-bit CRC

        // Split into two bytes
        byte highByte = (byte) ((crcValue >> 8) & 0xFF); // High byte
        byte lowByte = (byte) (crcValue & 0xFF);         // Low byte

        return new byte[]{highByte, lowByte}; // Return CRC as two bytes
    }

    //..................................................................


    public static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >> 24), // Most Significant Byte
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value // Least Significant Byte
        };
    }
    public static byte[] intToTwoBytes(int value) {
        return new byte[] {
                (byte) (value >> 8), // Most Significant Byte
                (byte) value         // Least Significant Byte
        };
    }
public static void syncPingnThrDLA() {
    while (isRunning()) {
        elapsedTimeSendThr = System.currentTimeMillis() - startTimeSendThr;
        elapsedTimeUplink = System.currentTimeMillis() - startTimeUplink;
        elapsedTimeDownlink = System.currentTimeMillis() - startTimeDownlink;
        // Send throttle command after 30ms
        if (elapsedTimeSendThr >= 40) {
             sendThrottleCommand();
             //System.out.println("Throttle Timer: " + elapsedTimeSendThr);
            startTimeSendThr = System.currentTimeMillis(); // Reset for next iteration

        }

        // Send other command after 200ms
        if (elapsedTimeUplink >= 400) {  // Changed == to >=
            initDLA();  // or initDLA() - whatever you want to send
            //System.out.println("Uplink Timer: " + elapsedTimeUplink);
            startTimeUplink = System.currentTimeMillis();

        }
        if (elapsedTimeDownlink >= 600) {


                // 1. Look for the sync pattern F1 1F
            // Look for your header byte (e.g., 0xE6 from your data)
            boolean foundHeader = false;
            int attempts = 0;

            while (!foundHeader && attempts < 200) {
                byte[] temp = new byte[1];
                arduinoPort.readBytes(temp, 1);

                if (temp[0] == (byte) 0xf1) { // Your header byte
                    Params.getInstance().getReceivingPackDLA()[0] = temp[0];
                    arduinoPort.readBytes(Params.getParam().getReceivingPackDLA(), 126, 1); // Read remaining 126 bytes
                    foundHeader = true;
                }
                attempts++;
            }
            if (foundHeader) {
                arduinoPort.readBytes(Params.getInstance().getReceivingPackDLA(), 127, 0);
                String bufferD = bytesToHexString(Params.getInstance().getReceivingPackDLA());
                System.out.println(bufferD);
                Params.getInstance().extractDLAPack2(Params.param.getReceivingPackDLA());
                startTimeDownlink=System.currentTimeMillis();
            }
           // break;
        }
    }
}




    public static String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for      (byte b : bytes){
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }
}
