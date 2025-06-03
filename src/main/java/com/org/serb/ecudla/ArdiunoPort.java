package com.org.serb.ecudla;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.util.zip.CRC32;

public class ArdiunoPort {
    public static String devicePortName = "COM14";
    public static SerialPort arduinoPort = null;
    public static InputStream arduinoStream = null;
    public static int PACKET_SIZE_IN_BYTES = 82;
    private static ArdiunoPort single_instance = null;
    static String logText = "";
    boolean status = false;

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

    public static void connectPort2(String port) {
        devicePortName = port;

        int len = SerialPort.getCommPorts().length;
        SerialPort serialPorts[] = new SerialPort[len];
        serialPorts = SerialPort.getCommPorts();

        for (int i = 0; i < len; i++) {

            String portName = serialPorts[i].getDescriptivePortName();
            System.out.println(serialPorts[i].getSystemPortName() + ": " + portName + ": " + i);
            loogger = new DataLogger();
            if (portName.contains(devicePortName)) {
                try {
                    arduinoPort = serialPorts[i];
                    arduinoPort.setBaudRate(115200);
                    arduinoPort.openPort();
                    setRunning(true);
                    System.out.println("connected to: " + portName + "[" + i + "]");
                    logText = "Connected to: " + portName;

                    loogger.start();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    // System.out.println("Not connected to: " + portName + "[" + i + "]");
                    loogger.stop();
                    arduinoPort.closePort();
                }
            }

        }

        //  (new Thread(new SerialReader(Params.getInstance().getReceivingPackSMD()))).start();

//--------give it a rest--------------------------------

        //arduinoPort.removeDataListener();
        // arduinoPort.closePort();
//        arduinoPort.addDataListener(new SerialPortDataListener() {
//            @Override
//            public int getListeningEvents() {
//                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//            }
//
//            @Override
//            public void serialEvent(SerialPortEvent event) {
//if(event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
// char[] buffer=new char[36];
//
//                BufferedReader portreader=new BufferedReader(new InputStreamReader(arduinoPort.getInputStream()));
//                try{
//                  //  portreader.read(buffer,0,36);
//                    String line=portreader.readLine();
//                    System.out.println(line);
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//                arduinoPort.readBytes(Params.getInstance().getReceivingPack(),36,0);
//                // if((Params.getInstance().getReceivingPack()[35] & 0xff)==13 && (Params.getInstance().getReceivingPack()[36] &0xff)==10)
//                Params.getInstance().extractDownlink(Params.getInstance().getReceivingPack());
//               // System.out.println(bytesToHexString(Params.getInstance().getReceivingPack()));
//                try {
//                    Thread.sleep(142);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
    }
    void startFeedbackThread() {
        (new Thread(new SerialReader(Params.getInstance().getReceivingPackSMD()))).start();
    }
    void startFeedbackThreadDLA() {
        (new Thread(new ThrottleCmndHandler(bytesThrottle))).start();
        (new Thread(new SerialReader(Params.getInstance().getReceivingPackDLAArduino()))).start();
    }
    void startPingThread() {
        new Thread(new SerialWriter()).start();
    }
    public static class SerialReader implements Runnable {
        byte[] buffer;                     //Anta fi Visa type gstreamer ... Put me aside of your sides 
        public SerialReader(byte[] buffer) {
            this.buffer = buffer;
            System.out.println("Reader");
        }
        public void run() {
            readDLAData(buffer);
           // readDLAviaArduino(buffer);
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

    public static void sendData(byte[] buffer) {
//        int i=0;
//        while (loopStatus){
//             i++;
        arduinoPort.writeBytes(Params.getInstance().getSendingPack(), 6, 0);
        System.out.println("Sending " + bytesToHexString(Params.getInstance().getSendingPack()));
        try {
            // Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
//            if(i>5) loopStatus=false;
//       }
    }


    public static void readData(byte[] buffer,boolean loopStatus){
        while (isRunning()){

            arduinoPort.readBytes(Params.getInstance().getReceivingPack(),38,0);
           // System.out.println(arduinoPort.getReadTimeout()+"  "+arduinoPort.getDeviceReadBufferSize());
           // System.out.println(Params.getInstance().getReceivingPack()[35] & 0xff);
            if((Params.getInstance().getReceivingPack()[0] & 0xff)==144 && (Params.getInstance().getReceivingPack()[1]& 0xff)==159 && (Params.getInstance().getReceivingPack()[2]& 0xff)==250 ) {
                if ((Params.getInstance().getReceivingPack()[37] & 0xff) == 187) {
                        Params.getInstance().extractDownlink(Params.getInstance().getReceivingPack());

                    String bufferD = bytesToHexString(Params.getInstance().getReceivingPack());
                 //   System.out.println(bufferD+"CRC>>"+getCRC(Params.getInstance().getReceivingPack())+"Buffer CRC>>"+Params.getInstance().getCrc());
                    Params.getInstance().setDownlinkBuffer(bufferD);
                    loogger.onEvent();
                }
            }
            try {
                Thread.sleep(500);   //from 20 to 200

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static void readDataSMD(byte[] buffer,boolean loopStatus){
        while (isRunning()){



          //  System.out.println(arduinoPort.getReadTimeout()+"  "+arduinoPort.getDeviceReadBufferSize());
            // System.out.println(Params.getInstance().getReceivingPack()[35] & 0xff);
          //  arduinoPort.readBytes(Params.getInstance().getReceivingPackSMD(),1);
            // if(Params.getInstance().getReceivingPackSMD()[0]==(byte)0x2d) { //Let me show another kind of systemology
            //if((Params.getInstance().getReceivingPackSMD()[0] & 0xff)==45 && (Params.getInstance().getReceivingPackSMD()[12]& 0xff)==106 ) {
                arduinoPort.readBytes(Params.getInstance().getReceivingPackSMD(),10,0);
                String bufferD = bytesToHexString(Params.getInstance().getReceivingPackSMD());
                System.out.println(bufferD);
                if(Params.getInstance().getReceivingPackSMD()[0]==0x2d && Params.getInstance().getReceivingPackSMD()[9]==0x6a) {
                    Params.getInstance().extractDownlinkSMD(Params.getInstance().getReceivingPackSMD());
                }

                // String bufferD = bytesToHexString(Params.getInstance().getReceivingPackSMD());
                //   System.out.println(bufferD+"CRC>>"+getCRC(Params.getInstance().getReceivingPack())+"Buffer CRC>>"+Params.getInstance().getCrc());
                //Params.getInstance().setDownlinkBuffer(bufferD);
                //loogger.onEvent();
           // }
            //}
            loogger.onEvent();
            try {
                Thread.sleep(200);      //from 50 to 200
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static byte[] send = new byte[]{(byte) 0xF1, 0x1F, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x22, 0x66, (byte) 0xD6, (byte) 0xF1, (byte) 0xF2, 0x2F};
    public static void initDLA(){
        arduinoPort.writeBytes(send, send.length, 0);
    }


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
    public  void sendThrottle(int val){

        byte[] ThrBytes = intToTwoBytes(val*10);
        bytesThrottle = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,
                0x12,0x00,0x00,0x22,0x77,0x32,0x05,0x02,0x00,ThrBytes[1],ThrBytes[0],0x00,0x00,0x00,0x00};
        int crcValue = CRC_Calculator.crcCalByByte(bytesThrottle,bytesThrottle.length-4);
        System.out.printf("Computed CRC: 0x%04X%n", crcValue);
        // Split into two bytes
        byte highByte = (byte) ((crcValue >> 8) & 0xFF); // High byte
        byte lowByte = (byte) (crcValue & 0xFF);         // Low byte

        bytesThrottle[16]=highByte;
        bytesThrottle[17]=lowByte;
        bytesThrottle[18]=(byte)0xf2;
        bytesThrottle[19]=0x2f;

//        if (isRunning()) {
//            arduinoPort.writeBytes(bytesThrottle, bytesThrottle.length, 0);
//            String bufferD = bytesToHexString(bytesThrottle);
//            System.out.println("Lookup CRC: "+bufferD);
//        }
//        try {
//
//            Thread.sleep(3);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public static void sendThrottleCommand(){
        do  {
            try {

                arduinoPort.writeBytes(bytesThrottle, bytesThrottle.length, 0);
                //String bufferD = bytesToHexString(bytesThrottle);
                //System.out.println("Lookup CRC: "+bufferD);

                Thread.sleep(30);

            }catch (Exception e){
                e.printStackTrace();
            }

        }while (isRunning());
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


    public static void readDLAData(byte[] buffer){
        do  {
            try {
            Thread.sleep(200);
            initDLA();                         //Handshake
            Thread.sleep(200);
            arduinoPort.readBytes(Params.getInstance().getReceivingPackDLA(), 127, 0);
            String bufferD = bytesToHexString(Params.getInstance().getReceivingPackDLA());
            //System.out.println(bufferD);
            //if (Params.getInstance().getReceivingPackDLA()[0] == 0x3E)
            Params.getInstance().extractDLAPack2(Params.param.getReceivingPackDLA());
            }catch (Exception e){
                e.printStackTrace();
            }

        }while (isRunning());
    }
    //
    public static void readDLAviaArduino(byte[] buffer){
        while (isRunning()) {
            arduinoPort.readBytes(Params.getInstance().getReceivingPackDLAArduino(), 17, 0);
            String bufferD = bytesToHexString(Params.getInstance().getReceivingPackDLAArduino());
            System.out.println(bufferD);
            if (Params.getInstance().getReceivingPackDLAArduino()[0] == 0x2d)
            Params.getInstance().extractDLAviaArduino(Params.param.getReceivingPackDLAArduino());
            loogger.onEvent();
            try {
                Thread.sleep(900);
            }catch (Exception e){
                e.printStackTrace();
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
