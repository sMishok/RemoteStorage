package ru.msakhterov.rs_client.view;

import ru.msakhterov.rs_common.Requests;
import ru.msakhterov.rs_common.SocketThread;
import ru.msakhterov.rs_common.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ClientGUI extends JFrame implements Thread.UncaughtExceptionHandler,
        ActionListener, SocketThreadListener {


    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final String WINDOW_TITLE = "Storage Client";
    private final JTextArea log = new JTextArea();
    private final JPanel panel = new JPanel(new GridLayout(9, 1));
    private final JTextField tfIPAddress = new JTextField("192.168.31.230");
    private final JTextField tfPort = new JTextField("8190");
    private final JTextField tfEmail = new JTextField("test1@test.ru");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("test");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnReg = new JButton("Registration");
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss: ");
    private int btn = 0;
    private SocketThread socketThread;

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

        panel.add(cbAlwaysOnTop);
        panel.add(tfIPAddress);
        panel.add(tfPort);
        panel.add(tfLogin);
        panel.add(tfPassword);
        panel.add(tfEmail);
        panel.add(btnLogin);
        panel.add(btnReg);
        panel.add(btnDisconnect);
        btnDisconnect.setVisible(false);
        add(panel, BorderLayout.EAST);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnLogin) {
            btn = 1;
            connect();
        } else if (src == btnReg) {
            btn = 2;
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
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

    private void connect() {
        Socket socket = null;
        try {
            socket = new Socket(tfIPAddress.getText(),
                    Integer.parseInt(tfPort.getText()));
        } catch (IOException e) {
            log.append("Exception: " + e.getMessage());
        }
        socketThread = new SocketThread(this, "SocketThread", socket);
    }

    private void putLog(String message) {
        log.append(message + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    void handleMessage(String value) {
        String[] arr = value.split(Requests.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Requests.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + ": " + arr[1]);
                break;
            case Requests.AUTH_DENIED:
                putLog(value);
                break;
            case Requests.REG_ACCEPT:
                setTitle(WINDOW_TITLE + ": " + arr[1]);
                break;
            case Requests.REG_DENIED:
                putLog(value);
                break;
            case Requests.REQUEST_FORMAT_ERROR:
                putLog(value);
                socketThread.close();
                break;
            default:
                throw new RuntimeException("Unknown message format: " + value);
        }
    }

    @Override
    public void onStartSocketThread(SocketThread thread, Socket socket) {
        putLog("Поток сокета стартовал");
    }

    @Override
    public void onStopSocketThread(SocketThread thread) {
        putLog("Соединение разорвано");
        setTitle(WINDOW_TITLE);
        panel.setVisible(true);
    }

    @Override
    public void onSocketIsReady(SocketThread thread, Socket socket) {
        putLog("Соединение установлено");
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        String email = tfEmail.getText();
        if (btn == 1) {
            thread.sendRequest(Requests.getAuthRequest(login, password));
        } else {
            thread.sendRequest(Requests.getRegRequest(login, password, email));
        }
        btnReg.setVisible(false);
        btnLogin.setVisible(false);
        btnDisconnect.setVisible(true);
    }

    @Override
    public void onReceiveRequest(SocketThread thread, Socket socket, String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMessage(value);
            }
        });
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Exception e) {

    }
}
