package ru.msakhterov.rs_client.controller;

public interface ClientListener {

    void onLogin();

    void onRegistration();

    void onDisconnect();

    void onUpload();

    void onUpload(String filepath);

    void onDownload(String fileName);

    void onDelete(String fileName);
}
