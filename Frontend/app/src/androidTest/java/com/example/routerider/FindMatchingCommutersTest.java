package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.example.routerider.fragments.RoutesFragment;

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

    // NO CHATGPT
    public void mockEvent(String addr) throws InterruptedException {
        ViewInteraction floatingButton = onView(withId(R.id.floating_action_button));
        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Route Event"), closeSoftKeyboard());

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText(addr), closeSoftKeyboard());

        ViewInteraction dateEditText = onView(withId(R.id.date_edit_text));
        dateEditText.perform(click());

        int eventHour = 10;
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventHour,0)));
        onView(withText("OK"))
                .perform(click());

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventHour,30)));
        onView(withText("OK"))
                .perform(click());

        onView(withText("OK")).perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
    }
    // PARTIAL CHATGPT
    public void clearEvents() throws InterruptedException {
        // onView(withId(R.id.schedule_tab)).perform(click());

        // Check if "Route Event" exists
        ViewInteraction displayEvent1Name = onView(withText("Route Event"));
        if (isViewDisplayed(displayEvent1Name)) {
            displayEvent1Name.perform(longClick());

            ViewInteraction okButton = onView(withId(android.R.id.button1));
            okButton.check(matches(isDisplayed()));
            okButton.check(matches(withText("OK")));
            okButton.perform(click());

            Thread.sleep(5000); // You may need to adjust the delay
        }
    }

    // YES CHATGPT
    private boolean isViewDisplayed(ViewInteraction viewInteraction) {
        try {
            viewInteraction.check(matches(isDisplayed()));
            return true;
        } catch (NoMatchingViewException e) {
            return false;
        }
    }

    // NO CHATGPT
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
        clearEvents();
        mockEvent("2424 Main Mall, Vancouver, BC V6T 1Z4");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction routeTab = onView(allOf(withContentDescription("Routes"),
                        withParent(withParent(withId(R.id.tab_layout))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
        routeTab.perform(click());

        // Force refresh UI
        onView(withId(R.id.next_day_route)).perform(click());
        Thread.sleep(1000); // You may need to adjust the delay
        onView(withId(R.id.previous_day_route)).perform(click());
        Thread.sleep(2000); // You may need to adjust the delay

        onView(withId(R.id.friend_button)).perform(click());
        onView(withText("Friend List")).check(matches(isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            onView(withText(friendName)).check(matches(isDisplayed()));
        }
        // ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        // matchingCommutersButton.check(matches(isDisplayed()));

        onView(withText(RoutesFragment.friendNames[0])).perform(click());

        Thread.sleep(5000);

        onView(withText("With " + RoutesFragment.friendNames[0])).check(matches(isDisplayed()));
    }

    // NO CHATGPT
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
        clearEvents();
        mockEvent("UBC MacLeod");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction routeTab = onView(allOf(withContentDescription("Routes"),
                        withParent(withParent(withId(R.id.tab_layout))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
        routeTab.perform(click());

        ViewInteraction transitFriendButton = onView(withId(R.id.friend_button));


        transitFriendButton.perform(click());
        Thread.sleep(1000); // You may need to adjust the delay
        onView(withText("Friend List")).check(matches(isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            onView(withText(friendName)).check(matches(isDisplayed()));
        }
        ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        matchingCommutersButton.check(matches(isDisplayed()));
        matchingCommutersButton.perform(click());
        Thread.sleep(5000); // You may need to adjust the delay

        onView(withText("Matching Commuters")).check(matches(isDisplayed()));

        // check that a textview with the @ symbol is displayed, then click it
        onView(withText(containsString("@")))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(500); // You may need to adjust the delay



        String expectedToastMessage1 = "Friend request sent";
        String expectedToastMessage2 = "Friend request already sent";
        String expectedToastMessage3 = "Already friends with this user";

        // check that any of the 3 expected messages above is displayed
        onView(anyOf(
                withText(containsString(expectedToastMessage1)),
                withText(containsString(expectedToastMessage2)),
                withText(containsString(expectedToastMessage3))
        ))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    // NO CHATGPT
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
        clearEvents();
        mockEvent("2500 Chem. de Polytechnique, Montreal, QC H3T 1J4");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction routeTab = onView(allOf(withContentDescription("Routes"),
                        withParent(withParent(withId(R.id.tab_layout))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
        routeTab.perform(click());

        ViewInteraction transitFriendButton = onView(withId(R.id.friend_button));

        transitFriendButton.check(matches(isEnabled()));

        transitFriendButton.perform(click());
        Thread.sleep(1000); // You may need to adjust the delay
        onView(withText("Friend List")).check(matches(isDisplayed()));
        for (String friendName : RoutesFragment.friendNames) {
            onView(withText(friendName)).check(matches(isDisplayed()));
        }
        ViewInteraction matchingCommutersButton = onView(withText("Find matching commuters"));
        matchingCommutersButton.check(matches(isDisplayed()));
        matchingCommutersButton.perform(click());
        Thread.sleep(500); // You may need to adjust the delay

        String expectedToastMessage = "No matching commuters found.";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));
    }
}
