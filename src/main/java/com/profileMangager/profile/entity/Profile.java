package com.profileMangager.profile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Profile {

    @Id
    private Long userId;      // khóa chính trong DB, tự generate trong service

    private String username;

    private boolean gender;

    private int age;

    // getter / setter

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {  // cần để JPA mapping + service set ID
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
