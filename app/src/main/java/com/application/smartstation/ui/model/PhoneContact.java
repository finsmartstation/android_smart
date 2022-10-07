package com.application.smartstation.ui.model;

import java.util.List;

public class PhoneContact {
    //contact id
    private int id;
    //contact id
    private String name;
    //contact numbers
    private List<String> phoneNumbers;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
