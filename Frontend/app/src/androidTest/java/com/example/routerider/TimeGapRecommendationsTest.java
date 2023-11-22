package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import static androidx.test.espresso.intent.Intents;
//import static androidx.test.espresso.intent.matcher.IntentMatchers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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

    public void mockEvents(int gap, String addr1, String addr2) throws InterruptedException {
        int[][] eventTimes = computeEventTimes(gap);

        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event 1"), ViewActions.closeSoftKeyboard());

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText(addr1), ViewActions.closeSoftKeyboard());

        ViewInteraction dateEditText = Espresso.onView(ViewMatchers.withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventTimes[0][0], eventTimes[0][1])));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventTimes[1][0], eventTimes[1][1])));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());


        Thread.sleep(5000); // You may need to adjust the delay

        floatingButton.perform(click());


        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event 2"), ViewActions.closeSoftKeyboard());

        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText(addr2), ViewActions.closeSoftKeyboard());

        dateEditText.perform(click());
        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventTimes[2][0], eventTimes[2][1])));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(eventTimes[3][0], eventTimes[3][1])));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());
    }
    public void clearEvents() throws InterruptedException {
        ViewInteraction displayEvent1Name = Espresso.onView(ViewMatchers.withText("Test Event 1"));
        displayEvent1Name.perform(ViewActions.longClick());

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay


        ViewInteraction displayEvent2Name = Espresso.onView(ViewMatchers.withText("Test Event 2"));
        displayEvent2Name.perform(ViewActions.longClick());

        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(click());
    }

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

            // If there's less than (3hrs + gap), throw an error to console
            if (totalTimeRemaining < (3 * 60 + gap)) {
                throw new IllegalArgumentException("Not enough time remaining in the day for the events.");
            }

            // Set start time of the first event to the start of the next hour
            int startHourOfFirstEvent = currentHour + 1;
            int startMinuteOfFirstEvent = 0;

            // Set end time of the first event 30 minutes later
            int endHourOfFirstEvent = startHourOfFirstEvent;
            int endMinuteOfFirstEvent = startMinuteOfFirstEvent + 30;

            // Set start time of the second event to end of first event + gap
            int startHourOfSecondEvent = endHourOfFirstEvent;
            int startMinuteOfSecondEvent = endMinuteOfFirstEvent + gap;

            // Set end time of the second event to 30 minutes later
            int endHourOfSecondEvent = startHourOfSecondEvent;
            int endMinuteOfSecondEvent = startMinuteOfSecondEvent + 30;

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
        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayTimeGap = Espresso.onView(ViewMatchers.withId(R.id.time_gap_text));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.withText("Gap: 00:30")));

        clearEvents();
    }

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
        mockEvents(10, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");
        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayTimeGap = Espresso.onView(ViewMatchers.withId(R.id.time_gap_text));
        displayTimeGap.check(ViewAssertions.doesNotExist());

        clearEvents();
    }

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

        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");

        ViewInteraction displayTimeGap = Espresso.onView(ViewMatchers.withId(R.id.time_gap_text));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.withText("Gap: 00:30")));

        ViewInteraction displayViewRecommendations = Espresso.onView(ViewMatchers.withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Hide Recommendations")));

        ViewInteraction displayHiddenView = Espresso.onView(allOf(ViewMatchers.withId(R.id.hidden_view), withParentIndex(0)));
        displayHiddenView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        int childCount = 0;
        ViewInteraction currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
        while (true) {
            try {
                currentChild.check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewException) {
                        // This will throw an exception if the view is not found, and we can exit the loop
                    }
                });
                childCount++;
                currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
            } catch (Exception e) {
                // Exit the loop when the view is not found
                break;
            }
        }

        assertThat(childCount, greaterThan(0));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay

        childCount = 0;
        currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
        while (true) {
            try {
                currentChild.check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewException) {
                    }
                });
                childCount++;
                currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
            } catch (Exception e) {
                // Exit the loop when the view is not found
                break;
            }
        }

        // Check that childCount == 0
        assertThat(childCount, equalTo(0));

        clearEvents();
    }

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
        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "6000 Student Union Blvd, Vancouver, BC V6T 1Z1");

        ViewInteraction displayTimeGap = Espresso.onView(ViewMatchers.withId(R.id.time_gap_text));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.withText("Gap: 03:00")));

        ViewInteraction displayViewRecommendations = Espresso.onView(ViewMatchers.withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Hide Recommendations")));

        Matcher<View> firstRecommendation = allOf(withParent(withId(R.id.hidden_view)), withParentIndex(0));
        ViewInteraction googleMapsButton = onView(allOf(withParent(firstRecommendation), withId(R.id.maps_button)));

        googleMapsButton.perform(ViewActions.click());

        // Verify that the correct intent was sent
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_VIEW));

        Thread.sleep(5000); // You may need to adjust the delay

        clearEvents();
    }

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

        mockEvents(30, "2424 Main Mall, Vancouver, BC V6T 1Z4", "367 George St, Sydney NSW 2000, Australia");
        Thread.sleep(5000); // You may need to adjust the delay


//        ViewInteraction nextDayButton = Espresso.onView(ViewMatchers.withId(R.id.next_day));
//        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        floatingButton.perform(click());
//        onView(ViewMatchers.withId(R.id.next_day)).perform(click());
//        Thread.sleep(5000); // You may need to adjust the delay
//
//        onView(ViewMatchers.withId(R.id.previous_day)).perform(click());




//
//        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
//        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Route Event")));
//
//        ViewInteraction displayEventLocation = Espresso.onView(ViewMatchers.withId(R.id.event_location));
//        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        displayEventLocation.check(ViewAssertions.matches(ViewMatchers.withText("2424 Main Mall, Vancouver, BC V6T 1Z4")));
//
//        ViewInteraction displayStartTime = Espresso.onView(ViewMatchers.withId(R.id.start_time));
//        displayStartTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        displayStartTime.check(ViewAssertions.matches(ViewMatchers.withText("12:00")));
//
//        ViewInteraction displayEndTime = Espresso.onView(ViewMatchers.withId(R.id.end_time));
//        displayEndTime.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        displayEndTime.check(ViewAssertions.matches(ViewMatchers.withText("14:00")));

//        ViewInteraction routesTabButton = Espresso.onView(ViewMatchers.withId(R.id.routes_tab_button));
//        routesTabButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        routesTabButton.perform(click());


        ViewInteraction displayTimeGap = Espresso.onView(ViewMatchers.withId(R.id.time_gap_text));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayTimeGap.check(ViewAssertions.matches(ViewMatchers.withText("Gap: 00:30")));

        ViewInteraction displayViewRecommendations = Espresso.onView(ViewMatchers.withId(R.id.view_recommendations_button));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Show Recommendations")));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay
        displayViewRecommendations.check(ViewAssertions.matches(ViewMatchers.withText("Hide Recommendations")));


        ViewInteraction displayHiddenView = Espresso.onView(allOf(ViewMatchers.withId(R.id.hidden_view), withParentIndex(0)));
        displayHiddenView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        int childCount = 0;
        ViewInteraction currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
        while (true) {
            try {
                currentChild.check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewException) {
                        // This will throw an exception if the view is not found, and we can exit the loop
                    }
                });
                childCount++;
                currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
            } catch (Exception e) {
                // Exit the loop when the view is not found
                break;
            }
        }

        assertThat(childCount, equalTo(0));

        displayViewRecommendations.perform(click());

        Thread.sleep(5000); // You may need to adjust the delay

        childCount = 0;
        currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
        while (true) {
            try {
                currentChild.check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewException) {
                    }
                });
                childCount++;
                currentChild = onView(allOf(withParent(withId(R.id.hidden_view)), withParentIndex(childCount)));
            } catch (Exception e) {
                // Exit the loop when the view is not found
                break;
            }
        }

        // Check that childCount == 0
        assertThat(childCount, equalTo(0));
        clearEvents();
        // ViewInteraction displayRecName = Espresso.onView(ViewMatchers.withId(R.id.rec_name));
    }
}
