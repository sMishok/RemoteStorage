package ru.msakhterov.rs_common;

public class Requests {

    public static final String DELIMITER = "§";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String REG_REQUEST = "/reg_request";
    public static final String REG_ACCEPT = "/reg_accept";
    public static final String REG_DENIED = "/reg_denied";
    public static final String REQUEST_FORMAT_ERROR = "/request_format_error";
    public static final String TYPE_REQUEST = "/request";

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String login) {
        return AUTH_ACCEPT + DELIMITER + login;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getRegRequest(String login, String password, String email) {
        return REG_REQUEST + DELIMITER + login + DELIMITER + password + DELIMITER + email;
    }

    public static String getRegAccept(String nickname) {
        return REG_ACCEPT + DELIMITER + nickname;
    }

    public static String getRegDenied() {
        return REG_DENIED;
    }

    public static String getReqestFormatError(String message) {
        return REQUEST_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeRequest(String src, String message) {
        return TYPE_REQUEST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

}
