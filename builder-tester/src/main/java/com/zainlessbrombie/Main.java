package com.zainlessbrombie;

import com.zainlessbrombie.cases.ReflectedObjectBuilder;
import com.zainlessbrombie.user.ExampleUser;
import com.zainlessbrombie.user.ExampleUserBuilder;
import com.zainlessbrombie.user.User;
import com.zainlessbrombie.user.UserBuilder;

public class Main {

    //@SmartBuilder


    public static void main(String[] args) {
        User user;


        user = UserBuilder.create()
                .active(true)
                .firstName("")
                .lastName("")
                .email("SOMEEMAIL")
                .password("")
                .roles(null)
                .language(null)
                .personnelNumber("")
                .transactionId("")
                .build();
        System.out.println(user.email);

        ReflectedObjectBuilder.create()
                .a("")
                .b(1)
                .c(new String[] {})
                .build();
    }
}
