package ru.msakhterov.rs_server.network;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;
    private ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        File rootDir = new File("RemoteStorageFiles\\");
        if (!rootDir.exists()) rootDir.mkdir();
        start();
    }

    @Override
    public void run() {
        listener.onStartServerSocketThread(this);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(timeout);
            listener.onCreateServerSocket(this, serverSocket);
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    listener.onAcceptTimeout(this, serverSocket);
                    continue;
                }
                listener.onSocketAccepted(this, socket);
            }
        } catch (IOException e) {
            listener.onServerSocketException(this, e);
        } finally {
            listener.onStopServerSocketThread(this);
        }
    }
}
