package com.example.routerider;

public class User {
    int userId;
    int[] friendList;

    public int[] getFriendList(int userId) {

        return null;
    }

    public int setPreferences(String preferenceName, int value) {
        return 1;
    }

    public int updateFriendList(int userId, String intent) {
        return 1;
    }

    public int updateProfile(String field, String newValue) {
        return 1;
    }

    public int authenticateUser(String login, String password) {
        return 1;
    }


}
