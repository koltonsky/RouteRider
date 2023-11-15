package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.util.Log;
import android.view.View;

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

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ManageCourseScheduleTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void addEventTest() throws InterruptedException {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            ViewInteraction loginToGoogle = onView(
                    allOf(withId(R.id.login_button), withText("LOGIN TO GOOGLE"),
                            withParent(withParent(withId(R.id.main_display))),
                            isDisplayed()));
            loginToGoogle.check(matches(isDisplayed()));
            loginToGoogle.perform(ViewActions.click());


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
            Thread.sleep(5000); // You may need to adjust the delay
        } catch (Exception e) {
            Log.d("Continue Test", "Already Logged In");
        }


        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(ViewActions.click());


        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText("Test Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Address")));

        ViewInteraction eventDateMonth = Espresso.onView(ViewMatchers.withId(R.id.event_date_month));
        eventDateMonth.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventDateMonth.perform(ViewActions.typeText("11"), ViewActions.closeSoftKeyboard());
        eventDateMonth.check(ViewAssertions.matches(ViewMatchers.withText("11")));

        ViewInteraction eventDateDay = Espresso.onView(ViewMatchers.withId(R.id.event_date_day));
        eventDateDay.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventDateDay.perform(ViewActions.typeText("15"), ViewActions.closeSoftKeyboard());
        eventDateDay.check(ViewAssertions.matches(ViewMatchers.withText("15")));

        ViewInteraction eventDateYear = Espresso.onView(ViewMatchers.withId(R.id.event_date_year));
        eventDateYear.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventDateYear.perform(ViewActions.typeText("2023"), ViewActions.closeSoftKeyboard());
        eventDateYear.check(ViewAssertions.matches(ViewMatchers.withText("2023")));

        ViewInteraction eventStartTime = Espresso.onView(ViewMatchers.withId(R.id.event_start_time));
        eventStartTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventStartTime.perform(ViewActions.typeText("17:00"), ViewActions.closeSoftKeyboard());
        eventStartTime.check(ViewAssertions.matches(ViewMatchers.withText("17:00")));

        ViewInteraction eventEndTime = Espresso.onView(ViewMatchers.withId(R.id.event_end_time));
        eventEndTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventEndTime.perform(ViewActions.typeText("19:00"), ViewActions.closeSoftKeyboard());
        eventEndTime.check(ViewAssertions.matches(ViewMatchers.withText("19:00")));

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(ViewActions.click());

        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));

        ViewInteraction displayEventLocation = Espresso.onView(ViewMatchers.withId(R.id.event_location));
        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.withText("Test Address")));

        ViewInteraction displayStartTime = Espresso.onView(ViewMatchers.withId(R.id.start_time));
        displayStartTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayStartTime.check(ViewAssertions.matches(ViewMatchers.withText("16:00")));

        ViewInteraction displayEndTime = Espresso.onView(ViewMatchers.withId(R.id.end_time));
        displayEndTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEndTime.check(ViewAssertions.matches(ViewMatchers.withText("18:00")));
    }

    @Test
    public void editEventTest() throws InterruptedException {
        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));
        displayEventName.perform(ViewActions.click());

        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.clearText());
        eventName.perform(ViewActions.typeText("Test Edit Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Event")));

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.clearText());
        eventAddress.perform(ViewActions.typeText("Test Edit Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Address")));

        ViewInteraction eventStartTime = Espresso.onView(ViewMatchers.withId(R.id.event_start_time));
        eventStartTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventStartTime.perform(ViewActions.clearText());
        eventStartTime.perform(ViewActions.typeText("18:00"), ViewActions.closeSoftKeyboard());
        eventStartTime.check(ViewAssertions.matches(ViewMatchers.withText("18:00")));

        ViewInteraction eventEndTime = Espresso.onView(ViewMatchers.withId(R.id.event_end_time));
        eventEndTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventEndTime.perform(ViewActions.clearText());
        eventEndTime.perform(ViewActions.typeText("20:00"), ViewActions.closeSoftKeyboard());
        eventEndTime.check(ViewAssertions.matches(ViewMatchers.withText("20:00")));

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(ViewActions.click());

        Thread.sleep(5000); // You may need to adjust the delay

        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Event")));

        ViewInteraction displayEventLocation = Espresso.onView(ViewMatchers.withId(R.id.event_location));
        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Address")));

        ViewInteraction displayStartTime = Espresso.onView(ViewMatchers.withId(R.id.start_time));
        displayStartTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayStartTime.check(ViewAssertions.matches(ViewMatchers.withText("18:00")));

        ViewInteraction displayEndTime = Espresso.onView(ViewMatchers.withId(R.id.end_time));
        displayEndTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEndTime.check(ViewAssertions.matches(ViewMatchers.withText("20:00")));
    }

    @Test
    public void deleteEventTest() throws InterruptedException {
        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Event")));
        displayEventName.perform(ViewActions.longClick());

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(ViewActions.click());

        Thread.sleep(5000); // You may need to adjust the delay

        displayEventName.check(ViewAssertions.doesNotExist());

    }
}
