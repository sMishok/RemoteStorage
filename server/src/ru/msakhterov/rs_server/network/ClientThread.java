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
    private boolean isAuthorized;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getUser() {
        return user;
    }

    public File getUserDir() {
        return new File("RemoteStorageFiles\\" + user);
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void authorizeAccept(String user) {
        isAuthorized = true;
        this.user = user;
        sendRequest(Requests.getAuthAccept(user));
        Object filesList = RequestService.getFilesList(this);
        if (filesList != null) {
            Object request = RequestMaker.makeFilesListRequest(Requests.getFilesListRequest(), RequestService.getFilesList(this));
            sendRequest(request);
        } else {
            sendRequest(Requests.getEmptyFilesDir());
        }


    }

    public void authorizeError() {
        sendRequest(Requests.getAuthDenied());
        close();
    }

    public void regError() {
        sendRequest(Requests.getRegDenied());
        close();
    }

    public void requestFormatError(String value) {
        sendRequest(Requests.getReqestFormatError(value));
        close();
    }
}
