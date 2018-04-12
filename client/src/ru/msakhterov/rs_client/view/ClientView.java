package ru.msakhterov.rs_client.view;

public interface ClientView {

    String getIP();

    int getPort();

    String getLogin();

    String getPassword();

    String getEmail();

    void logAppend(String msg);

    void setView(ViewStatement statement);

    void setViewTitle(String title);
}
