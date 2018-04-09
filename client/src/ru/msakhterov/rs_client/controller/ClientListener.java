package ru.msakhterov.rs_client.controller;

public interface ClientListener {

    public void onLogin();

    public void onRegistration();

    public void onDisconnect();

    public void onUpload();

    public void onDownload();


}
