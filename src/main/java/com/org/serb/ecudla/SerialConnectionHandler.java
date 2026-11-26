package com.org.serb.ecudla;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SerialConnectionHandler implements ConnectionHandler {

    private SerialPort arduinoPort = null;
    private InputStream arduinoStream = null;
    private boolean isConnected = false;

    @Override
    public boolean connect(String portName) throws IOException {
        System.out.println("SerialConnectionHandler: Attempting to connect to port: " + portName); // Added logging
        int len = SerialPort.getCommPorts().length;
        SerialPort serialPorts[] = SerialPort.getCommPorts();
        System.out.println("SerialConnectionHandler: Found " + len + " serial ports."); // Added logging
        for (int i = 0; i < len; i++) {
            String currentPortSystemName = serialPorts[i].getSystemPortName();
            String currentPortDescriptiveName = serialPorts[i].getDescriptivePortName();
            System.out.println("SerialConnectionHandler: Checking port " + i + ": SystemName=" + currentPortSystemName + ", DescriptiveName=" + currentPortDescriptiveName); // Added logging

            if (currentPortSystemName.equals(portName) || currentPortDescriptiveName.contains(portName)) { // Check both system and descriptive names
                try {
                    arduinoPort = serialPorts[i];
                    arduinoPort.setBaudRate(115200);
                    // Same mode as the working 4.5.0 port: return the bytes already
                    // waiting, instead of blocking until a whole requested buffer arrives.
                    arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                    System.out.println("SerialConnectionHandler: Opening port " + currentPortSystemName + " at 115200 baud.");
                    if (arduinoPort.openPort()) {
                        arduinoStream = arduinoPort.getInputStream();
                        isConnected = true;
                        System.out.println("SerialConnectionHandler: Successfully connected to: " + currentPortDescriptiveName + "[" + i + "]"); // Added logging
                        ArdiunoPort.logText = "Connected to: " + currentPortDescriptiveName;
                        return true;
                    } else {
                        System.err.println("SerialConnectionHandler: Failed to open port: " + currentPortDescriptiveName); // Added logging
                        throw new IOException("Failed to open serial port: " + currentPortDescriptiveName);
                    }
                } catch (Exception e) {
                    System.err.println("SerialConnectionHandler: Exception during port connection for " + currentPortDescriptiveName + ": " + e.getMessage()); // Added logging
                    e.printStackTrace();
                    throw new IOException("Failed to connect to serial port: " + e.getMessage(), e);
                }
            }
        }
        System.out.println("SerialConnectionHandler: No matching port found for: " + portName); // Added logging
        return false;
    }

    @Override
    public void disconnect() throws IOException {
        System.out.println("SerialConnectionHandler: Disconnect called."); // Added logging
        if (arduinoPort != null && arduinoPort.isOpen()) {
            System.out.println("SerialConnectionHandler: Closing port: " + arduinoPort.getSystemPortName()); // Added logging
            arduinoPort.closePort();
            isConnected = false;
            System.out.println("SerialConnectionHandler: Disconnected from serial port."); // Added logging
        } else {
            System.out.println("SerialConnectionHandler: No open port to disconnect."); // Added logging
        }
    }

    @Override
    public void sendData(byte[] data) throws IOException {
        // System.out.println("SerialConnectionHandler: Sending " + data.length + " bytes."); // Too verbose, uncomment for deep debug
        if (arduinoPort != null && arduinoPort.isOpen()) {
            arduinoPort.writeBytes(data, data.length, 0);
        } else {
            throw new IOException("Serial port not open. Cannot send data.");
        }
    }

    @Override
    public byte[] readData(int numBytes) throws IOException {
        if (arduinoPort == null || !arduinoPort.isOpen()) {
            throw new IOException("Serial port not open.");
        }
        int available = arduinoPort.bytesAvailable();
        if (available <= 0) {
            return new byte[0];
        }
        int toRead = Math.min(numBytes, available);
        byte[] buffer = new byte[toRead];
        int bytesRead = arduinoPort.readBytes(buffer, toRead);
        if (bytesRead <= 0) {
            return new byte[0];
        }
        if (bytesRead == buffer.length) {
            return buffer;
        }
        return Arrays.copyOf(buffer, bytesRead);
    }

    @Override
    public InputStream getInputStream() {
        return arduinoStream;
    }

    @Override
    public boolean isConnected() {
        return isConnected && arduinoPort != null && arduinoPort.isOpen();
    }

    public String[] getComPorts() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        String[] ports = new String[serialPorts.length];
        for (int i = 0; i < serialPorts.length; i++) {
            ports[i] = serialPorts[i].getSystemPortName();
        }
        return ports;
    }
}
