package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

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
public class TimeGapRecommendationsTest {

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
    public void mockEvents(int gap, String addr1, String addr2) throws InterruptedException {
        int[][] eventTimes = computeEventTimes(gap);

        ViewInteraction floatingButton = onView(withId(R.id.floating_action_button));
        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Test Event 1"), closeSoftKeyboard());

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText(addr1), closeSoftKeyboard());

        ViewInteraction dateEditText = onView(withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventTimes[0][0], eventTimes[0][1])));
        onView(withText("OK"))
                .perform(click());

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventTimes[1][0], eventTimes[1][1])));
        onView(withText("OK"))
                .perform(click());

        onView(withText("OK")).perform(click());


        Thread.sleep(5000); // You may need to adjust the delay

        floatingButton.perform(click());


        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Test Event 2"), closeSoftKeyboard());

        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText(addr2), closeSoftKeyboard());

        dateEditText.perform(click());
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventTimes[2][0], eventTimes[2][1])));
        onView(withText("OK"))
                .perform(click());

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(eventTimes[3][0], eventTimes[3][1])));
        onView(withText("OK"))
                .perform(click());

        onView(withText("OK")).perform(click());
    }
    // PARTIAL CHATGPT
    public void clearEvents() throws InterruptedException {
        // Check if Test Event 1 exists
        ViewInteraction displayEvent1Name = onView(withText("Test Event 1"));
        if (isViewDisplayed(displayEvent1Name)) {
            displayEvent1Name.perform(longClick());

            ViewInteraction okButton = onView(withId(android.R.id.button1));
            okButton.check(matches(isDisplayed()));
            okButton.check(matches(withText("OK")));
            okButton.perform(click());

            Thread.sleep(5000); // You may need to adjust the delay
        }

        // Check if Test Event 2 exists
        ViewInteraction displayEvent2Name = onView(withText("Test Event 2"));
        if (isViewDisplayed(displayEvent2Name)) {
            displayEvent2Name.perform(longClick());
            ViewInteraction okButton = onView(withId(android.R.id.button1));
            okButton.check(matches(isDisplayed()));
            okButton.check(matches(withText("OK")));
            okButton.perform(click());
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

    // YES CHATGPT
    private static int[][] computeEventTimes(int gap) {
        try {
            // Get current time from calendar
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            // Get total time between now and midnight of the same day
            int remainingHours = 24 - currentHour;
            int remainingMinutes = 60 - currentMinute;

            int totalTimeRemaining = remainingHours * 60 + remainingMinutes;

            // If there's less than (1hr + gap), throw an error to console
            if (totalTimeRemaining < (60 + gap)) {
                throw new IllegalArgumentException("Not enough time remaining in the day for the events.");
            }

            // Set start time of the first event to the start of the next hour
            int startHourOfFirstEvent = currentHour + 2;
            int startMinuteOfFirstEvent = 0;

            // Set end time of the first event 15 minutes later
            int endMinuteOfFirstEvent = (startMinuteOfFirstEvent + 15) % 60;
            int endHourOfFirstEvent = startHourOfFirstEvent;
            if (endMinuteOfFirstEvent < 15) {
                endHourOfFirstEvent += 1;
            }

            // Set start time of the second event to end of first event + gap
            int startMinuteOfSecondEvent = (endMinuteOfFirstEvent + gap) % 60;
            int startHourOfSecondEvent = endHourOfFirstEvent;
            if (startMinuteOfSecondEvent < gap) {
                startHourOfSecondEvent += 1;
            }

            // Set end time of the second event to 30 minutes later
            int endMinuteOfSecondEvent = (startMinuteOfSecondEvent + 15) % 60;
            int endHourOfSecondEvent = endHourOfFirstEvent;
            if (endMinuteOfSecondEvent < 15) {
                endHourOfSecondEvent += 1;
            }

            if (endHourOfSecondEvent == 24){
                endHourOfSecondEvent = 23;
                endMinuteOfSecondEvent = 59;
            }

            // Return as array of arrays of ints
            return new int[][]{
                    {startHourOfFirstEvent, startMinuteOfFirstEvent},
                    {endHourOfFirstEvent, endMinuteOfFirstEvent},
                    {startHourOfSecondEvent, startMinuteOfSecondEvent},
                    {endHourOfSecondEvent, endMinuteOfSecondEvent}
            };

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // NO CHATGPT
    @Test
    public void eventGapTest() throws InterruptedException {
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

        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayTimeGap = onView(withId(R.id.time_gap_text));
        displayTimeGap.check(matches(isDisplayed()));
        displayTimeGap.check(matches(withText("Gap: 00:30")));

    }


    // NO CHATGPT
    @Test
    public void noEventGapTest() throws InterruptedException {
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
        mockEvents(10, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayTimeGap = onView(withId(R.id.time_gap_text));
        displayTimeGap.check(doesNotExist());
    }

    // YES CHATGPT
    @Test
    public void recommendationsTest() throws InterruptedException {
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
        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");

        ViewInteraction displayTimeGap = onView(withId(R.id.time_gap_text));
        displayTimeGap.check(matches(isDisplayed()));
        displayTimeGap.check(matches(withText("Gap: 00:30")));

        ViewInteraction displayViewRecommendations = onView(withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(matches(isDisplayed()));
        displayViewRecommendations.check(matches(withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(2000); // You may need to adjust the delay
        displayViewRecommendations.check(matches(withText("Hide Recommendations")));

        Thread.sleep(2000); // You may need to adjust the delay

        onView(withId(R.id.hidden_timegap)).check(matches(hasMinimumChildCount(1)));
        displayViewRecommendations.perform(click());
        Thread.sleep(2000); // You may need to adjust the delay
        onView(withParent(withId(R.id.hidden_timegap))).check(doesNotExist());
    }


    // YES CHATGPT
    @Test
    public void googleMapsTest() throws InterruptedException {
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
        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");

        ViewInteraction displayTimeGap = onView(withId(R.id.time_gap_text));
        displayTimeGap.check(matches(isDisplayed()));
        displayTimeGap.check(matches(withText("Gap: 00:30")));

        ViewInteraction displayViewRecommendations = onView(withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(matches(isDisplayed()));
        displayViewRecommendations.check(matches(withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
        displayViewRecommendations.check(matches(withText("Hide Recommendations")));

        Matcher<View> firstRecommendation = allOf(withParent(withId(R.id.hidden_timegap)), withParentIndex(0));
        ViewInteraction googleMapsButton = onView(allOf(withParent(firstRecommendation), withId(R.id.maps_button)));
        Intents.init();

        googleMapsButton.perform(click());

        // Verify that the correct intent was sent
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_VIEW));
        Intents.release();

        // NOTE: Please manually move the Google Maps pop up window away from the bottom right corner or else all other tests will fail
    }

    // NO CHATGPT
    @Test
    public void noRecommendationsTest() throws InterruptedException {
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
        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "367 George St, Sydney NSW 2000, Australia");
        Thread.sleep(5000); // You may need to adjust the delay


        ViewInteraction displayTimeGap = onView(withId(R.id.time_gap_text));
        displayTimeGap.check(matches(isDisplayed()));
        displayTimeGap.check(matches(withText("Gap: 00:30")));

        ViewInteraction displayViewRecommendations = onView(withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(matches(isDisplayed()));
        displayViewRecommendations.check(matches(withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
        onView(withParent(withId(R.id.hidden_timegap))).check(doesNotExist());
    }
}
