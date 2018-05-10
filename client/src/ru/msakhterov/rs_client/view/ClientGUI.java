package ru.msakhterov.rs_client.view;

import ru.msakhterov.rs_client.controller.ClientController;
import ru.msakhterov.rs_client.controller.ClientListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
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
    private final Box leftPanelConnect = new Box(BoxLayout.Y_AXIS);
    private final JPanel leftPanelDisconnect = new JPanel(new GridLayout(8, 1));
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
    private final JButton btnRename = new JButton("Rename");

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private JScrollPane tableScrollPane;
    private ClientListener controller;
    private boolean isSelected = false;
    private int selectedRow;
    private String defaultPath;

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
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
        table.setRowSorter(sorter);
        ListSelectionModel selModel = table.getSelectionModel();
        selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        log.setRows(5);
        log.setEditable(false);
        log.setLineWrap(true);

        tableScrollPane = new JScrollPane(table);

        add(leftPanelConnect);

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
        btnRename.addActionListener(this);
        selModel.addListSelectionListener(this);

        controller = new ClientController(this);
        DragAndDropListener dnd = new DragAndDropListener(this);
        new DropTarget(tableScrollPane, dnd);
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
            clearTable();
        } else if (src == btnUpload) {
            controller.onUpload();
        } else if (src == btnDownload) {
            if (isSelected) {
                controller.onDownload(getSelectedFileName());
            }
            else JOptionPane.showMessageDialog(this, "Выберите файл для загрузки", "Загрузка файла", JOptionPane.INFORMATION_MESSAGE);
        } else if (src == btnDelete) {
            if (isSelected) {
                controller.onDelete(getSelectedFileName());
                isSelected = false;
            } else
                JOptionPane.showMessageDialog(this, "Выберите файл для удаления", "Удаление файла", JOptionPane.INFORMATION_MESSAGE);
        } else if (src == btnRename) {
            if (isSelected) {
                String[] temp = {getSelectedFileName()};
                String newFileName = JOptionPane.showInputDialog(this, "Введите новое имя файла", "Переименование файла", JOptionPane.INFORMATION_MESSAGE, null, null, temp[0]).toString();
                controller.onRename(getSelectedFileName(), newFileName);
                isSelected = false;
            } else
                JOptionPane.showMessageDialog(this, "Выберите файл для переименования", "Переименование файла", JOptionPane.INFORMATION_MESSAGE);
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    void uploadDraggedFile(String filePath) {
        controller.onUpload(filePath);
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
    public File getFilePath(String fileName) {
        JFileChooser fileChooser = new JFileChooser(defaultPath);
        File selectedFilePath = null;
        if (fileName == null) {
            fileChooser.setDialogTitle("Выбор файла");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFilePath = fileChooser.getSelectedFile();
                defaultPath = selectedFilePath.getParent();
            }
        }
         else {
                fileChooser.setDialogTitle("Сохранение файла");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setSelectedFile(new File(fileName));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    selectedFilePath = fileChooser.getSelectedFile();
                }
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
                leftPanelConnect.add(tableScrollPane);
                leftPanelConnect.add(new JScrollPane(log));
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
                rightPanel.add(btnRename);
                rightPanel.revalidate();
                repaint();
                break;
            case DISCONNECTED:
                leftPanelConnect.remove(tableScrollPane);
                leftPanelConnect.remove(new JScrollPane(log));
                rightPanel.remove(btnDisconnect);
                rightPanel.remove(btnUpload);
                rightPanel.remove(btnDownload);
                rightPanel.remove(btnDelete);
                rightPanel.remove(btnRename);
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
        clearTable();
        if (filesList != null) {
            for (int j = 0; j < filesList.length; j++) {
                Vector<String> row = new Vector<>();
                for (int i = 0; i < filesList[j].length; i++) {
                    row.add(filesList[j][i]);
                }
                tableModel.insertRow(j, row);
            }
        }
    }

    @Override
    public void setFilesList() {
        clearTable();
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

    private void clearTable() {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }
}
