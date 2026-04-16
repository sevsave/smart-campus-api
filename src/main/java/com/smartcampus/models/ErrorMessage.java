package com.smartcampus.models;

public class ErrorMessage {
    private String error;
    private int code;

    public ErrorMessage() {} // Default constructor for Jersey

    public ErrorMessage(String error, int code) {
        this.error = error;
        this.code = code;
    }

    // Getters (Required so the API can convert this to JSON)
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
} 