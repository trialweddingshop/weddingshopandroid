package com.example.weddingshop;

public class OrderDataModel {
    public String userName;
    public String itemName;
    public String cost;
    public OrderDataModel(String userName, String itemName, String cost){
        this.userName = userName;
        this.itemName = itemName;
        this.cost = cost;
    }
    public OrderDataModel(){

    }
}
