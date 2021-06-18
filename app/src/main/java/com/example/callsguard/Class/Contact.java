package com.example.callsguard.Class;


import java.io.Serializable;
import java.util.Objects;

public class Contact implements Serializable {
    private String name;
    private String number;
    private boolean isBusy;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
        this.isBusy = false;
    }
    public Contact(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(number, contact.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
