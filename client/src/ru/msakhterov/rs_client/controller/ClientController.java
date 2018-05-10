package ru.msakhterov.rs_client.controller;

import ru.msakhterov.rs_client.view.ClientView;
import ru.msakhterov.rs_client.view.ViewStatement;
import ru.msakhterov.rs_common.RequestMaker;
import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_common.SocketThread;
import ru.msakhterov.rs_common.SocketThreadListener;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.msakhterov.rs_common.Logger.putLog;

public class ClientController implements ClientListener, SocketThreadListener {

    private ClientView client;
    private SocketThread socketThread;
    private int selector = 0;

    public ClientController(ClientView client) {
        this.client = client;
    }

    @Override
    public void onLogin() {
        selector = 1;
        connect();
    }

    @Override
    public void onRegistration() {
        selector = 2;
        connect();
    }

    @Override
    public void onDisconnect() {
        socketThread.close();
    }

    @Override
    public void onUpload() {
        File selectedFile = client.getFilePath(null);
        if (selectedFile != null) {
            uploadFile(selectedFile);
        }
    }

    @Override
    public void onUpload(String filePath) {
        File selectedFile = new File(filePath);
        uploadFile(selectedFile);
    }

    @Override
    public void onDownload(String fileName) {
        socketThread.sendRequest(Requests.getDownloadRequest(fileName));
    }

    @Override
    public void onDelete(String fileName) {
        socketThread.sendRequest(Requests.getDeleteRequest(fileName));
    }

    @Override
    public void onRename(String oldFileName, String newFileName) {
        socketThread.sendRequest(Requests.getRenameRequest(oldFileName, newFileName));
    }

    private void connect() {
        Socket socket = null;
        try {
            socket = new Socket(client.getIP(), client.getPort());
        } catch (IOException e) {
            client.logAppend("Exception: " + e.getMessage());
        }
        socketThread = new SocketThread(this, "SocketThread", socket);
    }

    private void handleRequest(Object request) {
        if (request instanceof Object[]) {
            Object[] requestArr = (Object[]) request;
            String requestTitle = (String) requestArr[0];
            String[] arr = requestTitle.split(Requests.DELIMITER);
            String msgType = arr[0];
            switch (msgType) {
                case Requests.AUTH_ACCEPT:
                    client.setViewTitle(arr[1]);
                    client.logAppend("Успешная авторизация");
                    break;
                case Requests.AUTH_DENIED:
                    client.logAppend("Ошибка авторизации");
                    break;
                case Requests.FILES_LIST_REQUEST:
                    client.setFilesList((String[][]) requestArr[1]);
                    break;
                case Requests.EMPTY_FILES_DIR_REQUEST:
                    client.logAppend("Хранилище не содержит файлов");
                    client.setFilesList();
                    break;
                case Requests.REG_ACCEPT:
                    client.setViewTitle(arr[1]);
                    client.logAppend("Успешная авторизация");
                    break;
                case Requests.DOWNLOAD_ACCEPT:
                    downloadFile(requestArr[1], arr[1]);
                    break;
                case Requests.REG_DENIED:
                    client.logAppend("Ошибка регистрации");
                    break;
                case Requests.REQUEST_FORMAT_ERROR:
                    client.logAppend(requestTitle);
                    socketThread.close();
                    break;
                default:
                    throw new RuntimeException("Unknown message format: " + requestTitle);
            }
        }
    }

    @Override
    public void onStartSocketThread(SocketThread thread, Socket socket) {
        client.logAppend("Поток сокета стартовал");
    }

    @Override
    public void onStopSocketThread(SocketThread thread) {
        client.logAppend("Соединение разорвано");
        client.setViewTitle(null);
        client.setView(ViewStatement.DISCONNECTED);
    }

    @Override
    public void onSocketIsReady(SocketThread thread, Socket socket) {
        client.logAppend("Соединение установлено");
        String login = client.getLogin();
        String password = client.getPassword();
        String email = client.getEmail();
        if (selector == 1) {
            thread.sendRequest(Requests.getAuthRequest(login, password));
        } else {
            thread.sendRequest(Requests.getRegRequest(login, password, email));
        }
        client.setView(ViewStatement.CONNECTED);
    }

    @Override
    public void onReceiveRequest(SocketThread thread, Socket socket, Object request) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleRequest(request);
            }
        });
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Exception e) {
        client.logAppend(e.toString());
    }

    private void uploadFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer, 0, fis.available());
            Object request = RequestMaker.makeFileRequest(Requests.getUploadRequest(file.getName()), buffer);
            socketThread.sendRequest(request);
        } catch (IOException e) {
            client.logAppend("File load exception: " + e.getMessage());
        }
    }

    private void downloadFile(Object downloadFile, String fileName) {
        File selectedFile = client.getFilePath(fileName);
        if (selectedFile != null) {
            try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                byte[] buffer = (byte[]) downloadFile;
                fos.write(buffer, 0, buffer.length);

            } catch (IOException e) {
                putLog("Exception: " + e.getMessage());
            }
        }
    }
}


