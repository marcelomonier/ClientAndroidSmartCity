package com.example.clientsmartcity;



public class MessageEvent {

    private String data;

    public MessageEvent(String data){
        this.data = data;
    }

    public String getData(){
        return data;
    }
}