package ru.msakhterov.rs_common;

import java.net.Socket;

public interface SocketThreadListener {

    void onStartSocketThread(SocketThread thread, Socket socket);

    void onStopSocketThread(SocketThread thread);

    void onSocketIsReady(SocketThread thread, Socket socket);

    void onReceiveRequest(SocketThread thread, Socket socket, Object request);

    void onSocketThreadException(SocketThread thread, Exception e);

}
