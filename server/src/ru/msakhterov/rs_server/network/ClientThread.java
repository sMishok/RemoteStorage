package ru.msakhterov.rs_server.network;


import ru.msakhterov.rs_common.RequestMaker;
import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_common.SocketThread;
import ru.msakhterov.rs_common.SocketThreadListener;
import ru.msakhterov.rs_server.core.RequestService;

import java.io.File;
import java.net.Socket;

public class ClientThread extends SocketThread {

    private String user;
    private int userID;
    private boolean isAuthorized;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getUser() {
        return user;
    }

    public int getUserID() {
        return userID;
    }

    public File getUserDir() {
        return new File("RemoteStorageFiles\\" + userID);
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void authorizeAccept(String user, int userID) {
        isAuthorized = true;
        this.user = user;
        this.userID = userID;
        sendRequest(Requests.getAuthAccept(user));
    }

    public void authorizeError() {
        sendRequest(Requests.getAuthDenied());
        close();
    }

    public void regAccept(String user) {
        isAuthorized = true;
        this.user = user;
        sendRequest(Requests.getRegAccept(user));
    }

    public void regError() {
        sendRequest(Requests.getRegDenied());
        close();
    }

    public void requestFormatError(String value) {
        sendRequest(Requests.getReqestFormatError(value));
        close();
    }

    public void sendFileList(Object filesList) {
        if (filesList != null) {
            Object request = RequestMaker.makeFilesListRequest(Requests.getFilesListRequest(), RequestService.getFilesList(this));
            sendRequest(request);
        } else {
            sendRequest(Requests.getEmptyFilesDir());
        }
    }

}
