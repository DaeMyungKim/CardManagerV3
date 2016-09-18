package com.cardmanager.kdml.cardmanagerv3.DTO;

/**
 * Created by 김대명사무실 on 2016-07-29.
 */
public class User {
    public User(){}
    public User(String _email)
    {
        email = _email;
    }


    public String name;
    public String email;
    public String phone;
    public String fireBase_ID;

    public User(String email, String fireBase_ID, String name, String phone) {
        this.email = email;
        this.fireBase_ID = fireBase_ID;
        this.name = name;
        this.phone = phone;
    }

    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFireBase_ID() {
        return fireBase_ID;
    }

    public void setFireBase_ID(String fireBase_ID) {
        this.fireBase_ID = fireBase_ID;
    }
}
