package com.org.serb.ecudla;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;

public class UdpConnectionHandler implements ConnectionHandler {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean isConnected = false;
    private byte[] receiveBuffer = new byte[1024]; // A buffer for receiving UDP packets

    @Override
    public boolean connect(String connectionIdentifier) throws IOException {
        try {
            String[] parts = connectionIdentifier.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid UDP connection identifier format. Expected IP_ADDRESS:PORT");
            }
            this.address = InetAddress.getByName(parts[0]);
            this.port = Integer.parseInt(parts[1]);

            socket = new DatagramSocket(); // Binds to any available port
            socket.connect(address, port); // Connects the socket to the remote address and port
            socket.setSoTimeout(40);
            isConnected = true;
            System.out.println("Connected to UDP: " + connectionIdentifier);
            return true;
        } catch (SocketException | UnknownHostException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new IOException("Failed to connect to UDP: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            isConnected = false;
            System.out.println("Disconnected from UDP.");
        }
    }

    @Override
    public void sendData(byte[] data) throws IOException {
        if (isConnected && socket != null && !socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } else {
            throw new IOException("UDP socket not connected.");
        }
    }

    @Override
    public byte[] readData(int numBytes) throws IOException {
        if (isConnected && socket != null && !socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException timeout) {
                return new byte[0];
            }
            return Arrays.copyOf(packet.getData(), packet.getLength());
        } else {
            throw new IOException("UDP socket not connected.");
        }
    }

    @Override
    public InputStream getInputStream() {
        // For UDP, a traditional InputStream doesn't directly apply in the same way as TCP/Serial.
        // Data is received in discrete packets.
        // This implementation provides an InputStream that reads from the last received packet.
        // A more robust solution might involve a queue of received packets.
        try {
            byte[] data = readData(receiveBuffer.length); // Read a packet
            return new ByteArrayInputStream(data);
        } catch (IOException e) {
            e.printStackTrace();
            return new ByteArrayInputStream(new byte[0]); // Return empty stream on error
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
