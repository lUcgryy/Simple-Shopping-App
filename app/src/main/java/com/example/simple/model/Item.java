package com.example.simple.model;

public class Item {
    private String _id;
    private String name;
    private String description;
    private double price;
    private int amount;

    public Item() {
    }

    public Item(String _id, String name, String description, double price, int amount) {
        this._id = _id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
