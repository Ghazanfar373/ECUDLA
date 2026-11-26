package com.org.serb.ecudla;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * UDPCI client, matching the other ECU app: send to the target, but do not
 * connect() the socket. Gateways often answer from a different source port,
 * and a connected socket would drop those packets. A one-byte kick tells the
 * remote which port to answer.
 */
public class UdpCiConnectionHandler implements ConnectionHandler {

    private DatagramSocket socket;
    private volatile InetAddress address;
    private volatile int peerPort;
    private volatile boolean isConnected = false;

    @Override
    public boolean connect(String connectionIdentifier) throws IOException {
        String[] parts = connectionIdentifier == null ? new String[0] : connectionIdentifier.split(":");
        if (parts.length != 2) {
            throw new IOException("UDP address must look like x.x.x.x:port");
        }
        InetAddress resolved;
        int port;
        try {
            resolved = InetAddress.getByName(parts[0]);
            port = Integer.parseInt(parts[1]);
        } catch (UnknownHostException | NumberFormatException e) {
            throw new IOException("UDP address must look like x.x.x.x:port", e);
        }
        if (port < 1 || port > 65535) {
            throw new IOException("UDP port is out of range");
        }

        DatagramSocket opened = new DatagramSocket();
        try {
            opened.setSoTimeout(10);
            opened.send(new DatagramPacket(new byte[]{0}, 1, resolved, port));
        } catch (IOException e) {
            opened.close();
            throw new IOException("Could not open UDPCI to " + resolved.getHostAddress() + ":" + port + ": " + e.getMessage(), e);
        }
        this.socket = opened;
        this.address = resolved;
        this.peerPort = port;
        this.isConnected = true;
        InetSocketAddress local = (InetSocketAddress) opened.getLocalSocketAddress();
        System.out.println("UDPCI connected to " + resolved.getHostAddress() + ":" + port
                + " (local " + local.getAddress().getHostAddress() + ":" + local.getPort() + ", kicked)");
        return true;
    }

    @Override
    public void disconnect() {
        isConnected = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("UDPCI disconnected.");
        }
    }

    @Override
    public void sendData(byte[] data) throws IOException {
        DatagramSocket current = socket;
        InetAddress dest = address;
        if (!isConnected || current == null || current.isClosed() || dest == null) {
            throw new IOException("UDPCI socket not connected.");
        }
        current.send(new DatagramPacket(data, data.length, dest, peerPort));
    }

    @Override
    public byte[] readData(int numBytes) throws IOException {
        DatagramSocket current = socket;
        if (!isConnected || current == null || current.isClosed()) {
            return new byte[0];
        }
        byte[] buffer = new byte[Math.max(numBytes, 2048)];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            current.receive(packet);
        } catch (SocketTimeoutException | PortUnreachableException ignored) {
            return new byte[0];
        } catch (SocketException closed) {
            if (!isConnected || current.isClosed() || isReset(closed)) {
                return new byte[0];
            }
            throw closed;
        }
        int length = packet.getLength();
        if (length <= 0) {
            return new byte[0];
        }
        if (length == 1 && packet.getData()[packet.getOffset()] == 0) {
            return new byte[0];
        }
        InetAddress from = packet.getAddress();
        if (from != null && from.equals(address) && packet.getPort() != peerPort) {
            System.out.println("UDPCI reply port " + packet.getPort() + " (was " + peerPort + ")");
            peerPort = packet.getPort();
        }
        return Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + length);
    }

    private boolean isReset(SocketException error) {
        String message = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
        return message.contains("reset") || message.contains("10054");
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new ByteArrayInputStream(readData(2048));
        } catch (IOException e) {
            e.printStackTrace();
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
