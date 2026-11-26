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
    private volatile boolean isConnected = false;

    @Override
    public boolean connect(String connectionIdentifier) throws IOException {
        String[] parts = connectionIdentifier == null ? new String[0] : connectionIdentifier.split(":");
        if (parts.length != 2) {
            throw new IOException("UDP address must look like x.x.x.x:port");
        }
        try {
            this.address = InetAddress.getByName(parts[0]);
            this.port = Integer.parseInt(parts[1]);
        } catch (UnknownHostException | NumberFormatException e) {
            throw new IOException("UDP address must look like x.x.x.x:port", e);
        }
        if (port < 1 || port > 65535) {
            throw new IOException("UDP port is out of range");
        }

        // Talk to the remote port from our own local port. Binding the ECU's
        // port made this socket receive the commands it had just sent.
        DatagramSocket opened = new DatagramSocket();
        try {
            opened.connect(address, port);
            opened.setSoTimeout(10);
        } catch (IOException e) {
            opened.close();
            throw new IOException("Could not open UDP to " + address.getHostAddress() + ":" + port + ": " + e.getMessage(), e);
        }
        this.socket = opened;
        this.isConnected = true;
        System.out.println("UDP sending to " + address.getHostAddress() + ":" + port);
        return true;
    }

    @Override
    public void disconnect() {
        isConnected = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
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
        if (!isConnected || socket == null || socket.isClosed()) {
            return new byte[0];
        }
        byte[] buffer = new byte[Math.max(numBytes, 2048)];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException timeout) {
            return new byte[0];
        } catch (SocketException closed) {
            if (!isConnected || socket.isClosed()) {
                return new byte[0];
            }
            throw closed;
        }
        int length = packet.getLength();
        if (length <= 0) {
            return new byte[0];
        }
        return Arrays.copyOf(packet.getData(), length);
    }

    @Override
    public InputStream getInputStream() {
        // For UDP, a traditional InputStream doesn't directly apply in the same way as TCP/Serial.
        // Data is received in discrete packets.
        // This implementation provides an InputStream that reads from the last received packet.
        // A more robust solution might involve a queue of received packets.
        try {
            byte[] data = readData(2048);
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
