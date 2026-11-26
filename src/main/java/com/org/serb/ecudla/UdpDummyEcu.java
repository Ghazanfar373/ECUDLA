package com.org.serb.ecudla;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Loopback stand-in for the ECU. It listens on 127.0.0.1 and answers each
 * command with one 127-byte downlink frame, using the same header, field
 * layout, and CRC the dashboard already parses.
 */
public final class UdpDummyEcu {
    private static final int FRAME_LENGTH = 127;
    private static DatagramSocket socket;
    private static int boundPort = -1;
    private static Thread thread;

    private UdpDummyEcu() {
    }

    public static boolean isLoopback(String address) {
        return address != null && address.startsWith("127.0.0.1:");
    }

    public static synchronized void ensureStarted(String address) throws IOException {
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
        if (socket != null && !socket.isClosed() && boundPort == port) {
            return;
        }
        stop();
        DatagramSocket opened = new DatagramSocket(null);
        opened.setReuseAddress(true);
        opened.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
        opened.setSoTimeout(200);
        socket = opened;
        boundPort = port;
        thread = new Thread(() -> serve(opened), "udp-dummy-ecu");
        thread.setDaemon(true);
        thread.start();
        System.out.println("UDP dummy ECU listening on 127.0.0.1:" + port);
    }

    public static synchronized void stop() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
        boundPort = -1;
    }

    private static void serve(DatagramSocket opened) {
        byte[] incoming = new byte[512];
        InetAddress client = null;
        int clientPort = -1;
        int tick = 0;
        while (!opened.isClosed()) {
            DatagramPacket received = new DatagramPacket(incoming, incoming.length);
            try {
                opened.receive(received);
                client = received.getAddress();
                clientPort = received.getPort();
            } catch (SocketTimeoutException timeout) {
                // Keep sending once a dashboard has checked in.
            } catch (SocketException closed) {
                return;
            } catch (IOException e) {
                if (!opened.isClosed()) {
                    e.printStackTrace();
                }
                return;
            }
            if (client == null) {
                continue;
            }
            byte[] frame = frame(tick++);
            try {
                opened.send(new DatagramPacket(frame, frame.length, client, clientPort));
            } catch (IOException e) {
                if (!opened.isClosed()) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    static byte[] frame(int tick) {
        byte[] frame = new byte[FRAME_LENGTH];
        frame[0] = (byte) 0xF1;
        frame[1] = 0x1F;
        frame[5] = 0x7D;
        frame[89] = 0x01;

        int rpm = 28000 + (tick % 180) * 100;
        int tps = 150 + (tick % 40) * 10;
        int advance = 120 + (tick % 20);
        int injector = 1800 + (tick % 30) * 20;
        int fuelPerHour = 25 + (tick % 25);
        int temp1 = fahrenheitTenths(42 + (tick % 40) * 0.8);
        int temp2 = fahrenheitTenths(48 + (tick % 30) * 0.7);
        int intake = fahrenheitTenths(25);
        int baro = 1013;
        int volts = 126;
        float consumed = (float) (0.4 + tick * 0.001);

        putU16(frame, 15, rpm);
        putU16(frame, 33, tps);
        putU16(frame, 17, advance);
        putU16(frame, 11, injector);
        putU16(frame, 107, fuelPerHour);
        putU16(frame, 95, temp1);
        putU16(frame, 101, temp2);
        putU16(frame, 29, intake);
        putU16(frame, 25, baro);
        putU16(frame, 35, volts);
        putF32(frame, 91, consumed * 1.0e9f);
        putF32(frame, 119, tick / 5f);

        int crc = CRC_Calculator.crcCalByByte(frame, frame.length - 4);
        frame[123] = (byte) ((crc >> 8) & 0xFF);
        frame[124] = (byte) (crc & 0xFF);
        frame[125] = (byte) 0xF2;
        frame[126] = 0x2F;
        return frame;
    }

    public static void main(String[] args) {
        byte[] frame = frame(0);
        int crc = CRC_Calculator.crcCalByByte(frame, frame.length - 4);
        int stored = ((frame[123] & 0xFF) << 8) | (frame[124] & 0xFF);
        if (crc != stored || frame.length != 127 || frame[0] != (byte) 0xF1 || frame[1] != 0x1F) {
            System.err.println("Dummy frame does not match the ECU protocol.");
            System.exit(1);
        }
        System.out.println("Dummy frame OK, CRC 0x" + Integer.toHexString(crc));
    }

    private static int fahrenheitTenths(double celsius) {
        return (int) Math.round((celsius * 9.0 / 5.0 + 32.0) * 10.0);
    }

    private static void putU16(byte[] frame, int lowIndex, int value) {
        frame[lowIndex] = (byte) (value & 0xFF);
        frame[lowIndex + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private static void putF32(byte[] frame, int lowIndex, float value) {
        int bits = Float.floatToIntBits(value);
        frame[lowIndex] = (byte) (bits & 0xFF);
        frame[lowIndex + 1] = (byte) ((bits >> 8) & 0xFF);
        frame[lowIndex + 2] = (byte) ((bits >> 16) & 0xFF);
        frame[lowIndex + 3] = (byte) ((bits >> 24) & 0xFF);
    }
}
