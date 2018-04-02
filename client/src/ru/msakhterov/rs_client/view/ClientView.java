package ru.msakhterov.rs_client.view;

public interface ClientView {

    public String getIP();

    public int getPort();

    public String getLogin();

    public String getPassword();

    public String getEmail();

    public void logAppend(String msg);

    public void setView(ViewStatement statement);

    public void setViewTitle(String title);


}
