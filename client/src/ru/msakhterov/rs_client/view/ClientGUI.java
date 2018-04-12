package ru.msakhterov.rs_client.view;

import ru.msakhterov.rs_client.controller.ClientController;
import ru.msakhterov.rs_client.controller.ClientListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ClientGUI extends JFrame implements Thread.UncaughtExceptionHandler,
        ActionListener, ClientView {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    private static final String WINDOW_TITLE = "Storage Client";

    private final JTextArea log = new JTextArea();
    private final JPanel panel = new JPanel(new GridLayout(8, 1));
    private final JTextField tfIPAddress = new JTextField("localhost");
    private final JTextField tfPort = new JTextField("8190");
    private final JTextField tfEmail = new JTextField("test1@test.ru");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("test");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnReg = new JButton("Registration");
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JButton btnUpload = new JButton("Upload");
    private final JButton btnDownload = new JButton("Download");

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private ClientListener controller;

    public ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(WINDOW_TITLE);

        cbAlwaysOnTop.addActionListener(this);
        tfIPAddress.addActionListener(this);
        tfLogin.addActionListener(this);
        tfPassword.addActionListener(this);
        tfPort.addActionListener(this);
        tfEmail.addActionListener(this);
        btnLogin.addActionListener(this);
        btnReg.addActionListener(this);
        btnDisconnect.addActionListener(this);
        btnUpload.addActionListener(this);
        btnDownload.addActionListener(this);

        panel.add(cbAlwaysOnTop);
        panel.add(tfIPAddress);
        panel.add(tfPort);
        panel.add(tfLogin);
        panel.add(tfPassword);
        panel.add(tfEmail);
        panel.add(btnLogin);
        panel.add(btnReg);
        add(panel, BorderLayout.EAST);

        log.setRows(5);
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        controller = new ClientController(this);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnLogin) {
            controller.onLogin();
        } else if (src == btnReg) {
            controller.onRegistration();
        } else if (src == btnDisconnect) {
            controller.onDisconnect();
        } else if (src == btnUpload) {
            controller.onUpload();
        } else if (src == btnDownload) {
            controller.onDownload();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String message;
        if (stackTraceElements.length == 0) {
            message = "Empty Stacktrace";
        } else {
            message = e.getClass().getCanonicalName() +
                    ": " + e.getMessage() + "\n" +
                    "\t at " + stackTraceElements[0];
        }

        JOptionPane.showMessageDialog(this, message, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }


    @Override
    public String getIP() {
        return tfIPAddress.getText();
    }

    @Override
    public int getPort() {
        return Integer.parseInt(tfPort.getText());
    }

    @Override
    public String getLogin() {
        return tfLogin.getText();
    }

    @Override
    public String getPassword() {
        return new String(tfPassword.getPassword());
    }

    @Override
    public String getEmail() {
        return tfEmail.getText();
    }

    @Override
    public void logAppend(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) + ": " + msg;
        log.append(msg + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    @Override
    public void setView(ViewStatement statement) {
        switch (statement) {
            case CONNECTED:
                panel.remove(tfIPAddress);
                panel.remove(tfPort);
                panel.remove(tfLogin);
                panel.remove(tfPassword);
                panel.remove(tfEmail);
                panel.remove(btnLogin);
                panel.remove(btnReg);
                panel.add(btnDisconnect);
                panel.add(btnUpload);
                panel.add(btnDownload);
                panel.revalidate();
                repaint();
                break;
            case DISCONNECTED:
                panel.remove(btnDisconnect);
                panel.remove(btnUpload);
                panel.remove(btnDownload);
                panel.add(tfIPAddress);
                panel.add(tfPort);
                panel.add(tfLogin);
                panel.add(tfPassword);
                panel.add(tfEmail);
                panel.add(btnLogin);
                panel.add(btnReg);
                panel.revalidate();
                add(panel, BorderLayout.EAST);
                repaint();
            default:
                break;
        }
    }

    @Override
    public void setViewTitle(String title) {
        if (title != null) setTitle(WINDOW_TITLE + ": " + title);
        else setTitle(WINDOW_TITLE);
    }
}
