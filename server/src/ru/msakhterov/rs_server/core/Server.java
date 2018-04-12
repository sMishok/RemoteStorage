package ru.msakhterov.rs_server.core;

import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_common.SocketThread;
import ru.msakhterov.rs_common.SocketThreadListener;
import ru.msakhterov.rs_server.db.SqlClient;
import ru.msakhterov.rs_server.network.ClientThread;
import ru.msakhterov.rs_server.network.ServerSocketThread;
import ru.msakhterov.rs_server.network.ServerSocketThreadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static ru.msakhterov.rs_common.Logger.putLog;

public class Server implements ServerSocketThreadListener, SocketThreadListener {
    private ServerSocketThread serverSocketThread;
    private Vector<SocketThread> clients = new Vector<>();

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Сервер уже запущен");
        } else {
            serverSocketThread = new ServerSocketThread(this, "Server thread", port, 2000);
            SqlClient.connect();
        }
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Сервер не запущен");
        } else {
            serverSocketThread.interrupt();
            SqlClient.disconnect();
        }
    }

    public void showUsersData () {
            SqlClient.connect();
            String userData = SqlClient.showAllUsers();
            if (!userData.equals(null)) putLog(userData);
            else putLog("База данных не сожержит пользовательских данных");
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
                case Requests.UPLOAD_REQUEST:
                    File newFile = new File (client.getUserDir(), arr[1]);
                    try(FileOutputStream fos = new FileOutputStream(newFile)){
                        byte[] buffer = (byte[])requestArr[1];
                        fos.write(buffer, 0, buffer.length);
                        putLog("Пользователем " + client.getUser() + "загружен файл " + arr[1]);
                    } catch (IOException e){
                        putLog("Exception: " + e.getMessage());
                    }
                    break;

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
                    if (user == null) {
                        putLog("Некорректные login/password: login '" +
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
                    if (user == null) {
                        putLog("Email: " + email + "уже зарегистрирован");
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

