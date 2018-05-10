package ru.msakhterov.rs_common;

public class Requests {

    public static final String DELIMITER = "§";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String REG_REQUEST = "/reg_request";
    public static final String REG_ACCEPT = "/reg_accept";
    public static final String REG_DENIED = "/reg_denied";
    public static final String UPLOAD_REQUEST = "/upload_request";
    public static final String UPLOAD_ACCEPT = "/upload_accept";
    public static final String UPLOAD_DENIED = "/upload_denied";
    public static final String DOWNLOAD_REQUEST = "/download_request";
    public static final String DOWNLOAD_ACCEPT = "/download_accept";
    public static final String DELETE_REQUEST = "/delete_request";
    public static final String RENAME_REQUEST = "/rename_request";
    public static final String FILES_LIST_REQUEST = "/files_list_request";
    public static final String EMPTY_FILES_DIR_REQUEST = "/empty_files_dir_request";
    public static final String DOWNLOAD_DENIED = "/download_denied";
    public static final String REQUEST_FORMAT_ERROR = "/request_format_error";
    public static final String TYPE_REQUEST = "/request";

    //Авторизация
    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String login) {
        return AUTH_ACCEPT + DELIMITER + login;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    //Регистрация
    public static String getRegRequest(String login, String password, String email) {
        return REG_REQUEST + DELIMITER + login + DELIMITER + password + DELIMITER + email;
    }

    public static String getRegAccept(String nickname) {
        return REG_ACCEPT + DELIMITER + nickname;
    }

    public static String getRegDenied() {
        return REG_DENIED;
    }

    //Загрузка файла на сервер
    public static String getUploadRequest(String fileName) {
        return UPLOAD_REQUEST + DELIMITER + fileName;
    }

    public static String getUploadAccept() {
        return UPLOAD_ACCEPT;
    }

    public static String getUploadDenied() {
        return UPLOAD_DENIED;
    }

    //Загрузка файла с сервера
    public static String getDownloadRequest(String fileName) {
        return DOWNLOAD_REQUEST + DELIMITER + fileName;
    }

    public static String getDownloadAccept(String fileName) {
        return DOWNLOAD_ACCEPT + DELIMITER + fileName;
    }

    public static String getDownloadDenied() {
        return DOWNLOAD_DENIED;
    }

    //Удаление файла на сервере
    public static String getDeleteRequest(String fileName) {
        return DELETE_REQUEST + DELIMITER + fileName;
    }

    //Переименование файла на сервере
    public static String getRenameRequest(String oldFileName, String newFileName) {
        return RENAME_REQUEST + DELIMITER + oldFileName + DELIMITER + newFileName;
    }

    public static String getFilesListRequest() {
        return FILES_LIST_REQUEST;
    }

    public static String getEmptyFilesDir() {
        return EMPTY_FILES_DIR_REQUEST;
    }

    public static String getReqestFormatError(String message) {
        return REQUEST_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeRequest(String src, String message) {
        return TYPE_REQUEST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

}
