package com.waya.wayaauthenticationservice.config;

import lombok.Data;

@Data
public class LogMessage {

    private int httpStatus;
    private String httpMethod;
    private String path;
    private String clientIp;
    private String javaMethod;
    private String response;
    private long timeTakenMillionSecond;
    private String requestBody;
    private String requestParams;
    

    @Override
    public String toString() {
        return "{" + "\n" +
                "  httpStatus=" + httpStatus + "\n" +
                ", httpMethod='" + httpMethod + '\'' + "\n" +
                ", path='" + path + '\'' + "\n" +
                ", clientIp='" + clientIp + '\'' + "\n" +
                ", javaMethod='" + javaMethod + '\'' + "\n" +
                ", response='" + response + '\'' + "\n" +
                ", timeTakenMs='" + timeTakenMillionSecond + '\'' + "\n" +
                ", requestBody='" + requestBody + '\'' + "\n" +
                ", requestParams='" + requestParams + '\'' + "\n" +
                '}';
    }
}
