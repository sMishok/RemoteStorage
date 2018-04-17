package ru.msakhterov.rs_server.core;

import ru.msakhterov.rs_common.RequestMaker;
import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_server.db.SqlClient;
import ru.msakhterov.rs_server.network.ClientThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static ru.msakhterov.rs_common.Logger.putLog;

public class RequestService {

    public void checkNonAuthRequest(ClientThread client, Object request) {
        if (request instanceof Object[]) {
            Object[] requestArr = (Object[]) request;
            String requestTitle = (String) requestArr[0];
            String[] arr = requestTitle.split(Requests.DELIMITER);

            if (arr.length < 3) {
                client.requestFormatError(requestTitle);
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
                        client.authorizeError();
                        return;
                    }
                    client.authorizeAccept(user);
                    client.sendFileList(getFilesList(client));
                    break;
                case Requests.REG_REQUEST:
                    login = arr[1];
                    password = arr[2];
                    email = arr[3];
                    user = SqlClient.makeReg(login, password, email);
                    if (user == null) {
                        putLog("Email: " + email + "уже зарегистрирован");
                        client.regError();
                        return;
                    }
                    client.regAccept(user);
                    break;
                default:
                    client.requestFormatError(requestTitle);
                    break;
            }
        }
    }

    public void checkAuthRequest(ClientThread client, Object request) {
        if (request instanceof Object[]) {
            Object[] requestArr = (Object[]) request;
            String requestTitle = (String) requestArr[0];
            String[] arr = requestTitle.split(Requests.DELIMITER);
            String msgType = arr[0];
            switch (msgType) {
                case Requests.UPLOAD_REQUEST:
                    File uploadFile = new File(client.getUserDir(), arr[1]);
                    try (FileOutputStream fos = new FileOutputStream(uploadFile)) {
                        byte[] buffer = (byte[]) requestArr[1];
                        fos.write(buffer, 0, buffer.length);
                        putLog("Пользователем " + client.getUser() + " загружен файл " + arr[1]);
                        client.sendFileList(getFilesList(client));

                    } catch (IOException e) {
                        putLog("Exception: " + e.getMessage());
                    }
                    break;
                case Requests.DOWNLOAD_REQUEST:
                    File downloadFile = new File(client.getUserDir(), arr[1]);
                    try (FileInputStream fis = new FileInputStream(downloadFile)) {
                        byte[] buffer = new byte[fis.available()];
                        fis.read(buffer, 0, fis.available());
                        Object downloadAcceptRequest = RequestMaker.makeFileRequest(Requests.getDownloadAccept(downloadFile.getName()), buffer);
                        client.sendRequest(downloadAcceptRequest);
                    } catch (IOException e) {
                        putLog("Exception: " + e.getMessage());
                    }
                    break;
                case Requests.DELETE_REQUEST:
                    File deleteFile = new File(client.getUserDir(), arr[1]);
                    deleteFile.delete();
                    client.sendFileList(getFilesList(client));
                    break;

                case Requests.TYPE_REQUEST:
                    break;
                default:
                    client.requestFormatError(requestTitle);
            }
        }
    }

    public static Object getFilesList(ClientThread client) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yy HH:mm:ss");
        Object filesList = null;
        File userDir = client.getUserDir();
        File[] userFiles = userDir.listFiles();
        if (userFiles != null) {
            if (userFiles.length == 0) {
                return filesList;
            }
            String[][] filesArray = new String[userFiles.length][3];
            for (int i = 0; i < userFiles.length; i++) {
                filesArray[i][0] = userFiles[i].getName();
                filesArray[i][1] = Long.toString(userFiles[i].length());
                filesArray[i][2] = dateFormat.format((userFiles[i].lastModified()));
            }
            filesList = filesArray;
        }
        return filesList;
    }
}
