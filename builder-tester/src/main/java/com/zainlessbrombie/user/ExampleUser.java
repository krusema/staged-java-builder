package com.zainlessbrombie.user;


import com.zainlessbrombie.stagedbuilder.StagedBuilder;

@StagedBuilder
public class ExampleUser {
    public String username;
    public int age;
    public String password;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
