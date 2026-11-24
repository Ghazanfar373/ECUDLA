package com.org.serb.ecudla;

import java.io.IOException;
import java.io.InputStream;

public interface ConnectionHandler {
    boolean connect(String connectionIdentifier) throws IOException;
    void disconnect() throws IOException;
    void sendData(byte[] data) throws IOException;
    byte[] readData(int numBytes) throws IOException;
    InputStream getInputStream();
    boolean isConnected();
}
