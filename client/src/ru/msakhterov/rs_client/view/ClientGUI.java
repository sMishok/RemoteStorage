package ru.msakhterov.rs_client.view;

import ru.msakhterov.rs_client.controller.ClientController;
import ru.msakhterov.rs_client.controller.ClientListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ClientGUI extends JFrame implements Thread.UncaughtExceptionHandler,
        ActionListener, ListSelectionListener, ClientView {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    private static final String WINDOW_TITLE = "Storage Client";
    private final JPanel rightPanel = new JPanel(new GridLayout(8, 1));
    private final Box leftPanel = new Box(BoxLayout.Y_AXIS);
    private final DefaultTableModel tableModel = new DefaultTableModel();

    private final JTextArea log = new JTextArea();
    private final JTable table = new JTable();
    private Object[] columnsHeader = new String[]{"Наименование", "Размер", "Дата изменения"};
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
    private final JButton btnDelete = new JButton("Delete");

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private ClientListener controller;
    private boolean isSelected = false;
    private int selectedRow;

    public ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(WINDOW_TITLE);

        rightPanel.add(cbAlwaysOnTop);
        rightPanel.add(tfIPAddress);
        rightPanel.add(tfPort);
        rightPanel.add(tfLogin);
        rightPanel.add(tfPassword);
        rightPanel.add(tfEmail);
        rightPanel.add(btnLogin);
        rightPanel.add(btnReg);
        add(rightPanel, BorderLayout.EAST);

        tableModel.setColumnIdentifiers(columnsHeader);
        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnsWidth();
        ListSelectionModel selModel = table.getSelectionModel();
        selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        log.setRows(5);
        log.setEditable(false);
        log.setLineWrap(true);

        leftPanel.add(new JScrollPane(table));
        leftPanel.add(new JScrollPane(log));
        add(leftPanel);

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
        btnDelete.addActionListener(this);
        selModel.addListSelectionListener(this);

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
            if (isSelected)
            controller.onDownload(getSelectedFileName());
            else JOptionPane.showMessageDialog(this, "Выберите файл для загрузки", "Загрузка файла", JOptionPane.INFORMATION_MESSAGE);
        } else if (src == btnDelete) {
            if (isSelected)
                controller.onDelete(getSelectedFileName());
            else JOptionPane.showMessageDialog(this, "Выберите файл для удаления", "Удаление файла", JOptionPane.INFORMATION_MESSAGE);
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
    public File getFilePath(int selector) {
        JFileChooser fileChooser = new JFileChooser();
        File selectedFilePath = null;
        int result = 0;
        switch (selector){
            case 0:
                fileChooser.setDialogTitle("Выбор файла");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFilePath = fileChooser.getSelectedFile();
                }
                break;
            case 1:
                fileChooser.setDialogTitle("Выбор файла");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFilePath = fileChooser.getSelectedFile();
                }
                break;
        }
        return selectedFilePath;
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
                rightPanel.remove(tfIPAddress);
                rightPanel.remove(tfPort);
                rightPanel.remove(tfLogin);
                rightPanel.remove(tfPassword);
                rightPanel.remove(tfEmail);
                rightPanel.remove(btnLogin);
                rightPanel.remove(btnReg);
                rightPanel.add(btnDisconnect);
                rightPanel.add(btnUpload);
                rightPanel.add(btnDownload);
                rightPanel.add(btnDelete);
                rightPanel.revalidate();
                repaint();
                break;
            case DISCONNECTED:
                rightPanel.remove(btnDisconnect);
                rightPanel.remove(btnUpload);
                rightPanel.remove(btnDownload);
                rightPanel.remove(btnDelete);
                rightPanel.add(tfIPAddress);
                rightPanel.add(tfPort);
                rightPanel.add(tfLogin);
                rightPanel.add(tfPassword);
                rightPanel.add(tfEmail);
                rightPanel.add(btnLogin);
                rightPanel.add(btnReg);
                rightPanel.revalidate();
                add(rightPanel, BorderLayout.EAST);
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

    @Override
    public void setFilesList(String[][] filesList) {
        if (filesList != null) {
            while (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }
            for (int j = 0; j < filesList.length; j++) {
                Vector<String> row = new Vector<String>();
                for (int i = 0; i < filesList[j].length; i++) {
                    row.add((String) filesList[j][i]);
                }
                tableModel.insertRow(j, row);
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if ((selectedRow = table.getSelectedRow()) > -1) {
            isSelected = true;
        }
    }

    private String getSelectedFileName() {
        return tableModel.getValueAt(selectedRow, 0).toString();
    }

    private void setColumnsWidth() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader tableHeader = table.getTableHeader();
        int tableWidth = table.getWidth();
        System.out.println(tableWidth);
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            String columnName = tableHeader.getTable().getColumnName(i);
            switch (columnName) {
                case "Наименование":
                    column.setPreferredWidth(211);
                    break;
                case "Размер":
                    column.setPreferredWidth(55);
                    break;
                case "Дата изменения":
                    column.setPreferredWidth(120);
                    break;
            }
        }
    }
}
