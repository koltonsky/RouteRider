package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() throws InterruptedException {
        ViewInteraction loginToGoogle = onView(
                allOf(withId(R.id.login_button), withText("Login to Google"),
                        withParent(withParent(withId(R.id.main_display))),
                        isDisplayed()));
        loginToGoogle.check(matches(isDisplayed()));
        loginToGoogle.perform(ViewActions.click());

        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject2 test = uiDevice.findObject(By.text("koltonluu@gmail.com"));
        if (test != null) {
            test.click();
        } else {
            // Handle the case where the account is not found
            // You may want to use a different strategy or log an error
            Log.d("Account Error", "Failed to click account");
        }

        // Wait for the app to process the login
        uiDevice.waitForIdle();
        Thread.sleep(10000); // You may need to adjust the delay

        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
