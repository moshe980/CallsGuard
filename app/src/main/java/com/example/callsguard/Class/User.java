package com.example.callsguard.Class;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private static User instance;
    private String id;
    private ArrayList<Contact> contacts;
    private int contactsNum;

    public static void initUser(Context context, String id,int contactsNum, ArrayList<Contact> contacts) {
        if (instance == null) {
            instance = new User( context, id,contactsNum, contacts);
        }

    }

    private User(Context context, String id,int contactsNum, ArrayList<Contact> contacts) {
        this.id = id;
        this.contacts = contacts;
        this.contactsNum=contactsNum;
    }

    private User() {
    }

    public int getContactsNum() {
        return contactsNum;
    }

    public void setContactsNum(int contactsNum) {
        this.contactsNum = contactsNum;
    }

    public String getId() {
        return id;
    }

    public static User getInstance() {
        return instance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }
}
