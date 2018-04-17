package ru.msakhterov.rs_client.view;

import java.io.File;

public interface ClientView {

    String getIP();

    int getPort();

    String getLogin();

    String getPassword();

    String getEmail();

    File getFilePath(String fileName);

    void logAppend(String msg);

    void setView(ViewStatement statement);

    void setFilesList(String[][] filesList);

    void setViewTitle(String title);
}
