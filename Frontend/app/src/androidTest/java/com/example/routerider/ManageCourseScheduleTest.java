package com.example.routerider;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.icu.util.Calendar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageCourseScheduleTest {

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

    @After
    public void tearDown() {
        // Unregister the IdlingResource after the test
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    // YES CHATGPT
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

    // YES CHATGPT
    @Test
    public void a_addEventTest() throws InterruptedException {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            ViewInteraction loginToGoogle = onView(
                    allOf(withId(R.id.login_button), withText("LOGIN TO GOOGLE"),
                            withParent(withParent(withId(R.id.main_display))),
                            isDisplayed()));
            loginToGoogle.check(matches(isDisplayed()));
            loginToGoogle.perform(click());


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


        ViewInteraction floatingButton = onView(withId(R.id.floating_action_button));
        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Test Event"), closeSoftKeyboard());
        eventName.check(matches(withText("Test Event")));

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText("Test Address"), closeSoftKeyboard());
        eventAddress.check(matches(withText("Test Address")));

        ViewInteraction dateEditText = onView(withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 40)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_start_time))
                .check(matches(withText("23:40")));

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 45)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_end_time))
                .check(matches(withText("23:45")));

        onView(withText("OK")).perform(click());

        ViewInteraction displayEventName = onView(withId(R.id.event_name));
        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Event")));

        ViewInteraction displayEventLocation = onView(withId(R.id.event_location));
        displayEventLocation.check(matches(isDisplayed()));
        displayEventLocation.check(matches(withText("Test Address")));

        ViewInteraction displayStartTime = onView(withId(R.id.start_time));
        displayStartTime.check(matches(isDisplayed()));
        displayStartTime.check(matches(withText("23:40")));

        ViewInteraction displayEndTime = onView(withId(R.id.end_time));
        displayEndTime.check(matches(isDisplayed()));
        displayEndTime.check(matches(withText("23:45")));
    }

    // YES CHATGPT
    @Test
    public void b_editEventTest() throws InterruptedException {
        ViewInteraction displayEventName = onView(withId(R.id.event_name));
        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Event")));
        displayEventName.perform(click());

        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(clearText());
        eventName.perform(typeText("Test Edit Event"), closeSoftKeyboard());
        eventName.check(matches(withText("Test Edit Event")));

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(clearText());
        eventAddress.perform(typeText("Test Edit Address"), closeSoftKeyboard());
        eventAddress.check(matches(withText("Test Edit Address")));

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 41)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_start_time))
                .check(matches(withText("23:41")));

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 46)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_end_time))
                .check(matches(withText("23:46")));

        onView(withText("OK")).perform(click());

        Thread.sleep(5000); // You may need to adjust the delay

        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Edit Event")));

        ViewInteraction displayEventLocation = onView(withId(R.id.event_location));
        displayEventLocation.check(matches(isDisplayed()));
        displayEventLocation.check(matches(withText("Test Edit Address")));

        ViewInteraction displayStartTime = onView(withId(R.id.start_time));
        displayStartTime.check(matches(isDisplayed()));
        displayStartTime.check(matches(withText("23:41")));

        ViewInteraction displayEndTime = onView(withId(R.id.end_time));
        displayEndTime.check(matches(isDisplayed()));
        displayEndTime.check(matches(withText("23:46")));
    }

    // YES CHATGPT
    @Test
    public void c_deleteEventTest() throws InterruptedException {
        ViewInteraction displayEventName = onView(withId(R.id.event_name));
        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Edit Event")));
        displayEventName.perform(longClick());

        ViewInteraction okButton = onView(withId(android.R.id.button1));
        okButton.check(matches(isDisplayed()));
        okButton.check(matches(withText("OK")));
        okButton.perform(click());

        String expectedToastMessage = "Successfully deleted event";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

        displayEventName.check(doesNotExist());
    }

    // YES CHATGPT
    @Test
    public void d_changeEventDateTest() throws InterruptedException {
        ViewInteraction floatingButton = onView(withId(R.id.floating_action_button));
        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Test Event"), closeSoftKeyboard());
        eventName.check(matches(withText("Test Event")));

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText("Test Address"), closeSoftKeyboard());
        eventAddress.check(matches(withText("Test Address")));

        ViewInteraction dateEditText = onView(withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 50)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_start_time))
                .check(matches(withText("23:50")));

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 52)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_end_time))
                .check(matches(withText("23:52")));

        onView(withText("OK")).perform(click());


        Thread.sleep(5000); // You may need to adjust the delay

        ViewInteraction displayEventName = onView(withId(R.id.event_name));
        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Event")));

        ViewInteraction displayEventLocation = onView(withId(R.id.event_location));
        displayEventLocation.check(matches(isDisplayed()));
        displayEventLocation.check(matches(withText("Test Address")));

        ViewInteraction displayStartTime = onView(withId(R.id.start_time));
        displayStartTime.check(matches(isDisplayed()));
        displayStartTime.check(matches(withText("23:50")));

        ViewInteraction displayEndTime = onView(withId(R.id.end_time));
        displayEndTime.check(matches(isDisplayed()));
        displayEndTime.check(matches(withText("23:52")));

        displayEventName.check(matches(isDisplayed()));
        displayEventName.check(matches(withText("Test Event")));
        displayEventName.perform(click());

        eventName.check(matches(isDisplayed()));
        eventName.perform(clearText());
        eventName.perform(typeText("Test Edit Event"), closeSoftKeyboard());
        eventName.check(matches(withText("Test Edit Event")));

        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(clearText());
        eventAddress.perform(typeText("Test Edit Address"), closeSoftKeyboard());
        eventAddress.check(matches(withText("Test Edit Address")));

        dateEditText.perform(click());
        Calendar currentDate = Calendar.getInstance();

// Calculate the next day's date
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        int year = currentDate.get(Calendar.YEAR);
        int monthOfYear = currentDate.get(Calendar.MONTH) + 1;
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, monthOfYear, dayOfMonth));

        onView(withText("OK"))
                .perform(click());


        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 54)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_start_time))
                .check(matches(withText("23:54")));

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(23, 57)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_end_time))
                .check(matches(withText("23:57")));

        onView(withText("OK")).perform(click());

        displayEventName.check(doesNotExist());

        ViewInteraction nextDay = onView(withId(R.id.next_day));
        nextDay.check(matches(isDisplayed()));
        nextDay.perform(click());

        displayEventName.check(matches(withText("Test Edit Event")));
    }

    // YES CHATGPT
    @Test
    public void addEventFailureTest() {
        ViewInteraction floatingButton = onView(withId(R.id.floating_action_button));
        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());

        onView(withText("OK")).perform(click());
        onView(withText("Cancel")).perform(click());

        String expectedToastMessageOne = "Please fill out all fields";

        onView(withText(expectedToastMessageOne))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

        floatingButton.check(matches(isDisplayed()));
        floatingButton.perform(click());

        ViewInteraction eventName = onView(withId(R.id.event_name));
        eventName.check(matches(isDisplayed()));
        eventName.perform(typeText("Test Event"), closeSoftKeyboard());
        eventName.check(matches(withText("Test Event")));

        ViewInteraction eventAddress = onView(withId(R.id.event_address));
        eventAddress.check(matches(isDisplayed()));
        eventAddress.perform(typeText("Test Address"), closeSoftKeyboard());
        eventAddress.check(matches(withText("Test Address")));

        ViewInteraction dateEditText = onView(withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_start_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(11, 0)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_start_time))
                .check(matches(withText("11:00")));

        onView(withId(R.id.event_end_time))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(actionWithAssertions(PickerActions.setTime(11, 30)));
        onView(withText("OK"))
                .perform(click());
        onView(withId(R.id.event_end_time))
                .check(matches(withText("11:30")));

        onView(withText("OK")).perform(click());
        onView(withText("Cancel")).perform(click());

        String expectedToastMessage = "Please input proper time";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

    }
}
