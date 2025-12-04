package com.profileMangager.profile.dto.request;

public class ProfileUpdateRequest {

    private boolean gender;
    private int age;



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
