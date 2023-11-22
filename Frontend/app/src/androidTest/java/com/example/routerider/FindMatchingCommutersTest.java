package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.example.routerider.fragments.RoutesFragment;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Calendar;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FindMatchingCommutersTest {

    private View decorView;
    private IdlingResource idlingResource;

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        idlingResource = new ElapsedTimeIdlingResource(2000); // Set the timeout to 2 seconds
        IdlingRegistry.getInstance().register(idlingResource);
        mActivityScenarioRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

    private static class ElapsedTimeIdlingResource implements IdlingResource {

        private final long startTime;
        private final long waitingTime;
        private ResourceCallback resourceCallback;

        public ElapsedTimeIdlingResource(long waitingTime) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = waitingTime;
        }

        @Override
        public String getName() {
            return ElapsedTimeIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = elapsed >= waitingTime;

            if (idle && resourceCallback != null) {
                resourceCallback.onTransitionToIdle();
            }

            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

    public void mockEvent(String addr) throws InterruptedException {
        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Route Event"), ViewActions.closeSoftKeyboard());

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText(addr), ViewActions.closeSoftKeyboard());

        ViewInteraction dateEditText = Espresso.onView(ViewMatchers.withId(R.id.date_edit_text));
        dateEditText.perform(click());

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int eventHour = 6;
        if (currentHour > 5) {
             eventHour = currentHour + 1;
        }

        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventHour,0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventHour,30)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());


        Thread.sleep(5000); // You may need to adjust the delay
    }
    public void clearEvents() throws InterruptedException {
        onView(withId(R.id.schedule_tab_button)).perform(click());

        ViewInteraction displayEvent1Name = Espresso.onView(ViewMatchers.withText("Route Event"));
        displayEvent1Name.perform(ViewActions.longClick());

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
    }


    @Test
    public void friendTransitTest() throws InterruptedException {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            ViewInteraction loginToGoogle = onView(
                    allOf(withId(R.id.login_button), withText("LOGIN TO GOOGLE"),
                            withParent(withParent(withId(R.id.main_display))),
                            isDisplayed()));
            loginToGoogle.check(matches(isDisplayed()));
            loginToGoogle.perform(click());


            UiObject2 test = uiDevice.findObject(By.text("bannnamichael@gmail.com"));
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
        mockEvent("2424 Main Mall, Vancouver, BC V6T 1Z4");
        Thread.sleep(5000); // You may need to adjust the delay

        onView(withId(R.id.routes_tab_button)).perform(click());

        ViewInteraction transitFriendButton = onView(withId(R.id.transit_friend_button));

        transitFriendButton.check(matches(isEnabled()));

        transitFriendButton.perform(click());
        onView(withText("Friend List")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            onView(ViewMatchers.withText(friendName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
        ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        matchingCommutersButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withText(RoutesFragment.friendNames[0])).perform(click());

        Thread.sleep(5000);

        onView(ViewMatchers.withText("With " + RoutesFragment.friendNames[0])).check(matches(isDisplayed()));

        clearEvents();
    }

    @Test
    public void matchingCommuterTest() throws InterruptedException {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            ViewInteraction loginToGoogle = onView(
                    allOf(withId(R.id.login_button), withText("LOGIN TO GOOGLE"),
                            withParent(withParent(withId(R.id.main_display))),
                            isDisplayed()));
            loginToGoogle.check(matches(isDisplayed()));
            loginToGoogle.perform(click());


            UiObject2 test = uiDevice.findObject(By.text("bannnamichael@gmail.com"));
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
        mockEvent("2424 Main Mall, Vancouver, BC V6T 1Z4");
        Thread.sleep(5000); // You may need to adjust the delay

        onView(withId(R.id.routes_tab_button)).perform(click());

        ViewInteraction transitFriendButton = onView(withId(R.id.transit_friend_button));

        transitFriendButton.check(matches(isEnabled()));

        transitFriendButton.perform(click());
        Thread.sleep(1000); // You may need to adjust the delay
        onView(withText("Friend List")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            Espresso.onView(ViewMatchers.withText(friendName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
        ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        matchingCommutersButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        matchingCommutersButton.perform(click());
        Thread.sleep(5000); // You may need to adjust the delay

        onView(withText("Matching commuters")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        for (String commuterName : RoutesFragment.matchingCommuters) {
            Espresso.onView(ViewMatchers.withText(commuterName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
        onView(ViewMatchers.withText(RoutesFragment.matchingCommuters[0])).perform(click());

        Thread.sleep(5000);

        String expectedToastMessage = "Friend request sent";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));


        clearEvents();
    }

    @Test
    public void noMatchingCommuterTest() throws InterruptedException {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            ViewInteraction loginToGoogle = onView(
                    allOf(withId(R.id.login_button), withText("LOGIN TO GOOGLE"),
                            withParent(withParent(withId(R.id.main_display))),
                            isDisplayed()));
            loginToGoogle.check(matches(isDisplayed()));
            loginToGoogle.perform(click());


            UiObject2 test = uiDevice.findObject(By.text("bannnamichael@gmail.com"));
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
        mockEvent("2424 Main Mall, Vancouver, BC V6T 1Z4");
        Thread.sleep(5000); // You may need to adjust the delay

        onView(withId(R.id.routes_tab_button)).perform(click());

        ViewInteraction transitFriendButton = onView(withId(R.id.transit_friend_button));

        transitFriendButton.check(matches(isEnabled()));

        transitFriendButton.perform(click());
        Thread.sleep(1000); // You may need to adjust the delay
        onView(withText("Friend List")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            Espresso.onView(ViewMatchers.withText(friendName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
        ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        matchingCommutersButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        matchingCommutersButton.perform(click());
        Thread.sleep(5000); // You may need to adjust the delay

        String expectedToastMessage = "No matching commuters found";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

        onView(withText("Friend List")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            Espresso.onView(ViewMatchers.withText(friendName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
        matchingCommutersButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));


        clearEvents();
    }
}
