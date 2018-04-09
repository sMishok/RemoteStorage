package ru.msakhterov.rs_server.core;

import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_common.SocketThread;
import ru.msakhterov.rs_common.SocketThreadListener;
import ru.msakhterov.rs_server.db.SqlClient;
import ru.msakhterov.rs_server.network.ClientThread;
import ru.msakhterov.rs_server.network.ServerSocketThread;
import ru.msakhterov.rs_server.network.ServerSocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static ru.msakhterov.rs_common.Logger.putLog;

public class Server implements ServerSocketThreadListener, SocketThreadListener {
    private ServerSocketThread serverSocketThread;
    private Vector<SocketThread> clients = new Vector<>();

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Server is already running");
        } else {
            serverSocketThread = new ServerSocketThread(this, "Server thread", port, 2000);
            SqlClient.connect();
        }
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Server is not running");
        } else {
            serverSocketThread.interrupt();
            SqlClient.disconnect();
        }
    }

    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("Сервер запущен");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("Cервер остановлен");
    }

    @Override
    public void onCreateServerSocket(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("Cоздан Server socket");
    }

    @Override
    public void onAcceptTimeout(ServerSocketThread thread, ServerSocket serverSocket) {
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, Socket socket) {
        putLog("Клиент подключился: " + socket);
        String threadName = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, threadName, socket);
    }

    @Override
    public void onServerSocketException(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    @Override
    public synchronized void onStartSocketThread(SocketThread thread, Socket socket) {
        putLog("запущен");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread thread) {
        putLog("остановлен");
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
    }

    @Override
    public synchronized void onSocketIsReady(SocketThread thread, Socket socket) {
        putLog("готов к передаче данных");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveRequest(SocketThread thread, Socket socket, Object request) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthRequest(client, request);
        } else {
            handleNonAuthRequest(client, request);
        }
    }

    @Override
    public synchronized void onSocketThreadException(SocketThread thread, Exception e) {
        e.printStackTrace();
    }

    private ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getUser().equals(nickname))
                return client;
        }
        return null;
    }

    private void handleAuthRequest(ClientThread client, Object request) {
        if (request instanceof Object[]) {
            Object[] requestArr = (Object[]) request;
            String requestTitle = (String) requestArr[0];
            String[] arr = requestTitle.split(Requests.DELIMITER);
            String msgType = arr[0];
            switch (msgType) {
                case Requests.TYPE_REQUEST:
                    break;
                default:
                    client.requestFormatError(requestTitle);
            }
        }
    }

    private void handleNonAuthRequest(ClientThread newClient, Object request) {
        if (request instanceof Object[]) {
            Object[] requestArr = (Object[]) request;
            String requestTitle = (String) requestArr[0];
            String[] arr = requestTitle.split(Requests.DELIMITER);

            if (arr.length < 3) {
                newClient.requestFormatError(requestTitle);
                return;
            }
            String login;
            String password;
            String email;
            String user;
            switch (arr[0]) {
                case Requests.AUTH_REQUEST:
                    login = arr[1];
                    password = arr[2];
                    user = SqlClient.checkAuth(login, password);
                    System.out.println("!!!Авторизация логин: " + user);
                    if (user == null) {
                        putLog("Invalid login/password: login '" +
                                login + "' password: '" + password + "'");
                        newClient.authorizeError();
                        return;
                    }
                    newClient.authorizeAccept(user);
                    break;
                case Requests.REG_REQUEST:
                    login = arr[1];
                    password = arr[2];
                    email = arr[3];
                    user = SqlClient.makeReg(login, password, email);
                    System.out.println("!!!Регистрация логин: " + user);
                    if (user == null) {
                        putLog("Invalid login/password: login '" +
                                login + "' password: '" + password + "'");
                        newClient.regError();
                        return;
                    }
                    newClient.authorizeAccept(user);
                    break;
                default:
                    newClient.requestFormatError(requestTitle);
                    break;
            }
        }
    }
}

