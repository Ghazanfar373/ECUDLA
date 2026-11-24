package com.org.serb.ecudla;

import com.fazecast.jSerialComm.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

public class ArdiunoPort {
    public static String devicePortName = "COM14"; // Default serial port name
    private ConnectionHandler connectionHandler; // Use the interface
    public static int PACKET_SIZE_IN_BYTES = 127; // Adjusted based on your UDP output length
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

    // Internal buffer for incoming data
    private static ByteArrayOutputStream receiveBuffer = new ByteArrayOutputStream();


    public static boolean isRunning() {
        if (single_instance != null && single_instance.connectionHandler != null) {
            return single_instance.connectionHandler.isConnected();
        }
        return false;
    }
    public static void setRunning(boolean running) {
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

    public void setConnectionHandler(ConnectionHandler handler) {
        System.out.println("ArdiunoPort: Setting connection handler to: " + (handler != null ? handler.getClass().getSimpleName() : "null")); // Added logging
        this.connectionHandler = handler;
        // Clear buffer when connection handler changes
        synchronized (receiveBuffer) {
            receiveBuffer.reset();
        }
    }

    // New getter for the connectionHandler
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    static DataLogger loogger;
    boolean connectPort(String port) {
        System.out.println("ArdiunoPort: connectPort() called with port: " + port); // Added logging
        if (connectionHandler == null) {
            System.err.println("ArdiunoPort: ConnectionHandler not set. Cannot connect.");
            return false;
        }
        try {
            status = connectionHandler.connect(port);
            if (status) {
                System.out.println("ArdiunoPort: connected to: " + port);
                logText = "Connected to: " + port;
                // Create HexData Object
                Params.getInstance(); // Assuming Params is initialized here
                loogger = new DataLogger(); // Assuming DataLogger is initialized here
                loogger.start();
            } else {
                System.out.println("ArdiunoPort: Connection handler reported failure for port: " + port); // Added logging
            }
        } catch (IOException e) {
            System.err.println("ArdiunoPort: IOException during connectPort for " + port + ": " + e.getMessage()); // Added logging
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public void disconnectPort() {
        System.out.println("ArdiunoPort: disconnectPort() called."); // Added logging
        if (connectionHandler != null) {
            try {
                connectionHandler.disconnect();
            } catch (IOException e) {
                System.err.println("ArdiunoPort: IOException during disconnectPort: " + e.getMessage()); // Added logging
                e.printStackTrace();
            }
        } else {
            System.out.println("ArdiunoPort: No connection handler to disconnect."); // Added logging
        }
    }

    // This method should probably be moved to SerialConnectionHandler or a utility class
    // if other connection types don't need to list COM ports.
    public String[] getComPorts() {
        System.out.println("ArdiunoPort: getComPorts() called."); // Added logging
        if (connectionHandler instanceof SerialConnectionHandler) {
            return ((SerialConnectionHandler) connectionHandler).getComPorts();
        }
        System.out.println("ArdiunoPort: Current handler is not SerialConnectionHandler. Returning empty array."); // Added logging
        return new String[0]; // Or throw an UnsupportedOperationException
    }

    void startFeedbackThreadDLA() {
        System.out.println("ArdiunoPort: startFeedbackThreadDLA() called."); // Added logging
        // The SerialReader now needs access to the connectionHandler's InputStream
        // or needs to be refactored to use readData method.
        // For now, I'll pass the connectionHandler to the SerialReader.
        (new Thread(new SerialReader(Params.getInstance().getReceivingPackDLAArduino(), connectionHandler))).start();
    }

    public static class SerialReader implements Runnable {
        byte[] buffer;
        ConnectionHandler handler;

        public SerialReader(byte[] buffer, ConnectionHandler handler) {
            this.buffer = buffer;
            this.handler = handler;
            System.out.println("SerialReader: Initialized."); // Added logging
        }
        public void run() {
            System.out.println("SerialReader: run() started."); // Added logging
            // syncPingnThrDLA needs to be updated to use the handler
            syncPingnThrDLA(handler);
            System.out.println("SerialReader: run() finished."); // Added logging
        }
    }
    public static class SerialWriter implements Runnable {
        ConnectionHandler handler;
        public SerialWriter(ConnectionHandler handler) {
            this.handler = handler;
            System.out.println("SerialWriter: Initialized."); // Added logging
        }
        public void run() {
            System.out.println("SerialWriter: run() started."); // Added logging
            // pingDLAEFI needs to be updated to use the handler
            pingDLAEFI(handler);
            System.out.println("SerialWriter: run() finished."); // Added logging
        }
    }
    static byte[] send = new byte[]{(byte) 0xF1, 0x1F, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x22, 0x66, (byte) 0xD6, (byte) 0xF1, (byte) 0xF2, 0x2F};
    public static void initDLA(ConnectionHandler handler){
        System.out.println("ArdiunoPort: initDLA() called."); // Added logging
        try {
            handler.sendData(send);
        } catch (IOException e) {
            System.err.println("ArdiunoPort: IOException during initDLA: " + e.getMessage()); // Added logging
            e.printStackTrace();
        }
    }

//F1 1F 00 00 00 0C 00 00 22 66 D6 F1 F2 2F
    static byte[] cmdShutdownEngine = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,0x0E,0x00,0x00,0x22,0x71,0x71,0x78,0x22,0x58, (byte) 0xF2,0x2F};

    static byte[] cmdStartEngine = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,0x0E,0x00,0x00,0x22,0x71,0x71,0x64, (byte) 0xF1, (byte) 0xE5, (byte) 0xF2,0x2F};
    public void shutDownDLA(){
        System.out.println("ArdiunoPort: shutDownDLA() called."); // Added logging
        if(isRunning() && connectionHandler != null) {
            try {
                connectionHandler.sendData(cmdShutdownEngine);
                System.out.println("ShutDown CMND sent");
            } catch (IOException e) {
                System.err.println("ArdiunoPort: IOException during shutDownDLA: " + e.getMessage()); // Added logging
                e.printStackTrace();
            }
        } else {
            System.out.println("ArdiunoPort: Not running or no handler to shut down DLA."); // Added logging
        }
    }
    public void startDLA(){
        System.out.println("ArdiunoPort: startDLA() called."); // Added logging
        if(isRunning() && connectionHandler != null) {
            try {
                connectionHandler.sendData(cmdStartEngine);
                System.out.println("Start CMND sent");
            } catch (IOException e) {
                System.err.println("ArdiunoPort: IOException during startDLA: " + e.getMessage()); // Added logging
                e.printStackTrace();
            }
        } else {
            System.out.println("ArdiunoPort: Not running or no handler to start DLA."); // Added logging
        }
    }
    public static void pingDLAEFI(ConnectionHandler handler) {
        System.out.println("ArdiunoPort: pingDLAEFI() started."); // Added logging
        while (handler.isConnected()) {
            try {
                handler.sendData(send); // Assuming 'send' is the ping command
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                System.err.println("ArdiunoPort: Error in pingDLAEFI: " + e.getMessage()); // Added logging
                e.printStackTrace();
                break; // Exit loop on error
            }
        }
        System.out.println("ArdiunoPort: pingDLAEFI() finished."); // Added logging
    }
    static byte[] bytesThrottle = new byte[]{(byte) 0xF1,0x1F,0x00,0x00,0x00,
            0x12,0x00,0x00,0x22,0x77,0x32,0x05,0x02,0x00,0x00,0x00,0x00,0x00,(byte)0xf2,0x2f};
    public  void updateThrottleBuffer(int val) {
        System.out.println("ArdiunoPort: updateThrottleBuffer() called with value: " + val); // Added logging
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
    public static void sendThrottleCommand(ConnectionHandler handler){
            System.out.println("ArdiunoPort: sendThrottleCommand() called."); // Added logging
            try {
                handler.sendData(bytesThrottle);
                //String bufferD = bytesToHexString(bytesThrottle);
               // System.out.println("Throttle Command "+bufferD);

                Thread.sleep(10);

            }catch (Exception e){
                System.err.println("ArdiunoPort: Error in sendThrottleCommand: " + e.getMessage()); // Added logging
                e.printStackTrace();
            }
    }
    public static class ThrottleCmndHandler implements Runnable {
        byte[] buffer;
        ConnectionHandler handler;
        public ThrottleCmndHandler(byte[] buffer, ConnectionHandler handler) {
            this.buffer= buffer;
            this.handler = handler;
            System.out.println("ThrottleCmndHandler: Initialized."); // Added logging
        }
        public void run() {
            System.out.println("ThrottleCmndHandler: run() started."); // Added logging
            sendThrottleCommand(handler);
            System.out.println("ThrottleCmndHandler: run() finished."); // Added logging
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
public static void syncPingnThrDLA(ConnectionHandler handler) {
    System.out.println("ArdiunoPort: syncPingnThrDLA() started."); // Added logging
    byte[] readBuffer = new byte[512]; // Temporary buffer for reading from handler

    while (handler.isConnected()) {
        elapsedTimeSendThr = System.currentTimeMillis() - startTimeSendThr;
        elapsedTimeUplink = System.currentTimeMillis() - startTimeUplink;
        elapsedTimeDownlink = System.currentTimeMillis() - startTimeDownlink;

        // Send throttle command after 30ms
        if (elapsedTimeSendThr >= 40) {
             sendThrottleCommand(handler);
            startTimeSendThr = System.currentTimeMillis(); // Reset for next iteration
        }

        // Send other command after 200ms
        if (elapsedTimeUplink >= 400) {
            initDLA(handler);
            startTimeUplink = System.currentTimeMillis();
        }

        // Drain whatever the link has already received. A blocking read here
        // stalled the throttle commands and, on serial, timed out as end-of-stream.
        try {
            byte[] incomingBytes = handler.readData(readBuffer.length);
            if (incomingBytes.length > 0) {
                    synchronized (receiveBuffer) {
                        receiveBuffer.write(incomingBytes);
                    }
                    // System.out.println("ArdiunoPort: Received " + incomingBytes.length + " bytes. Buffer size: " + receiveBuffer.size()); // Debugging
                }

                // Process data from the receive buffer
                synchronized (receiveBuffer) {
                    byte[] currentBuffer = receiveBuffer.toByteArray();
                    int bufferLength = currentBuffer.length;

                    // Look for the header F1 1F
                    int headerIndex = -1;
                    for (int i = 0; i < bufferLength - 1; i++) {
                        if (currentBuffer[i] == (byte) 0xF1 && currentBuffer[i+1] == (byte) 0x1F) {
                            headerIndex = i;
                            break;
                        }
                    }

                    if (headerIndex != -1) {
                        // Found header, check if we have a full packet
                        if (bufferLength - headerIndex >= PACKET_SIZE_IN_BYTES) {
                            byte[] packet = Arrays.copyOfRange(currentBuffer, headerIndex, headerIndex + PACKET_SIZE_IN_BYTES);

                            // Process the packet
                            System.arraycopy(packet, 0, Params.getInstance().getReceivingPackDLA(), 0, PACKET_SIZE_IN_BYTES);
                            String bufferD = bytesToHexString(Params.getInstance().getReceivingPackDLA());
                            System.out.println(bufferD);
                            Params.getInstance().extractDLAPack2(Params.param.getReceivingPackDLA());
                            startTimeDownlink = System.currentTimeMillis(); // Reset timer after successful packet processing

                            // Remove the processed packet from the buffer
                            receiveBuffer.reset();
                            if (headerIndex + PACKET_SIZE_IN_BYTES < bufferLength) {
                                receiveBuffer.write(currentBuffer, headerIndex + PACKET_SIZE_IN_BYTES, bufferLength - (headerIndex + PACKET_SIZE_IN_BYTES));
                            }
                        } else {
                            // Header found, but not enough bytes for a full packet yet. Wait for more data.
                            // System.out.println("ArdiunoPort: Header found, but not enough bytes for full packet. Current buffer size: " + bufferLength + ", Header index: " + headerIndex); // Debugging
                        }
                    } else {
                        // No header found, or partial header at the end. Discard old data if buffer grows too large.
                        if (bufferLength > PACKET_SIZE_IN_BYTES * 2) { // Prevent buffer from growing indefinitely with garbage
                            System.out.println("ArdiunoPort: No header found, discarding " + (bufferLength - PACKET_SIZE_IN_BYTES) + " bytes from buffer."); // Debugging
                            receiveBuffer.reset();
                            receiveBuffer.write(currentBuffer, bufferLength - PACKET_SIZE_IN_BYTES, PACKET_SIZE_IN_BYTES); // Keep last part in case header is split
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("ArdiunoPort: Error in syncPingnThrDLA downlink: " + e.getMessage());
                e.printStackTrace();
            }

        // Small delay to prevent busy-waiting if no data is coming
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ArdiunoPort: syncPingnThrDLA interrupted.");
            break;
        }
    }
    System.out.println("ArdiunoPort: syncPingnThrDLA() finished."); // Added logging
}




    public static String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for      (byte b : bytes){
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }

    // --- Demonstration Main Method ---
    public static void main(String[] args) {
        ArdiunoPort arduino = ArdiunoPort.getInstance();

        // --- Example 1: Using Serial Connection ---
        System.out.println("Attempting to connect via Serial...");
        ConnectionHandler serialHandler = new SerialConnectionHandler();
        arduino.setConnectionHandler(serialHandler);
        if (arduino.connectPort("COM14")) { // Replace "COM14" with your actual Arduino serial port
            System.out.println("Serial connection established.");
            // You can now use arduino.startDLA(), arduino.shutDownDLA(), etc.
            // For demonstration, let's just disconnect after a short delay.
            try {
                Thread.sleep(2000); // Keep connection open for 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            arduino.disconnectPort();
            System.out.println("Serial connection disconnected.");
        } else {
            System.out.println("Failed to establish serial connection.");
        }

        System.out.println("\n--- Example 2: Using UDP Connection ---");
        // For UDP, you need an IP address and port of the target device.
        // This example assumes a UDP server is listening on localhost:12345
        ConnectionHandler udpHandler = new UdpConnectionHandler();
        arduino.setConnectionHandler(udpHandler);
        if (arduino.connectPort("127.0.0.1:12345")) { // Replace with your UDP target IP:Port
            System.out.println("UDP connection established.");
            // For demonstration, send a dummy command and then disconnect.
            try {
                byte[] dummyCommand = "Hello UDP!".getBytes();
                arduino.connectionHandler.sendData(dummyCommand);
                System.out.println("Sent dummy UDP data.");
                Thread.sleep(2000); // Keep connection open for 2 seconds
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            arduino.disconnectPort();
            System.out.println("UDP connection disconnected.");
        } else {
            System.out.println("Failed to establish UDP connection.");
        }
    }
}
