package com.example.routerider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

// NO CHATGPT
public class User {
    int userId;
    String name;
    String email;
    String address;
    int[] friendList;
    static GoogleSignInAccount currentAccount = null;

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

    public static void updateGoogleAccount(GoogleSignInAccount account) {
        currentAccount = account;
    }

    public static GoogleSignInAccount getCurrentAccount() {
        return currentAccount;
    }
}
