package ru.msakhterov.rs_common;

public class RequestMaker {

    public static Object makeRequest(String message, byte[] file){
        Object[] requestArr = new Object[2];
        requestArr[0] = message;
        requestArr[1] = file;
        return requestArr;
    }
}
