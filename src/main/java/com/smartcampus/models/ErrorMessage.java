/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.models;
/**
 *
 * @author Sewmini Senevirathna / 20240079 / w2149627
 */

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