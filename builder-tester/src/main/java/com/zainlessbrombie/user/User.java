package com.zainlessbrombie.user;

import com.zainlessbrombie.stagedbuilder.*;

import java.util.List;

@StagedBuilder(
        fieldAccessMethod = BuilderFieldAccessMethod.SETTER,
        validator = "")
public class User {
    public boolean active;

    @BuilderIgnored
    public String alias;

    @Nullable
    @Deprecated
    public String displayName;

    public String firstName;

    public String lastName;

    public String email;

    public String password;

    @Nullable
    public String personnelNumber;

    public List<UserRole> roles;

    public UserLanguage language;

    @BuilderOptional
    public String transactionId;

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    public void setLanguage(UserLanguage language) {
        this.language = language;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void validate() {
    }
}
