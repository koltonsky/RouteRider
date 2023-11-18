package com.example.routerider;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.icu.util.Calendar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.hamcrest.Matchers;
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

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        mActivityScenarioRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

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


        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText("Test Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Address")));

        ViewInteraction dateEditText = Espresso.onView(ViewMatchers.withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(17, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("17:00")));

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(19, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("19:00")));

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());


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
    public void b_editEventTest() throws InterruptedException {
        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));
        displayEventName.perform(click());

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

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(18, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("18:00")));

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(20, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("20:00")));

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

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
    public void c_deleteEventTest() throws InterruptedException {
        ViewInteraction displayEventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Event")));
        displayEventName.perform(ViewActions.longClick());

        ViewInteraction okButton = Espresso.onView(ViewMatchers.withId(android.R.id.button1));
        okButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        okButton.check(ViewAssertions.matches(ViewMatchers.withText("OK")));
        okButton.perform(click());



        String expectedToastMessage = "Successfully deleted event";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

        Thread.sleep(5000); // You may need to adjust the delay

        displayEventName.check(ViewAssertions.doesNotExist());
    }

    @Test
    public void d_changeEventDateTest() throws InterruptedException {
        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());


        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText("Test Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Address")));

        ViewInteraction dateEditText = Espresso.onView(ViewMatchers.withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(17, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("17:00")));

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(19, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("19:00")));

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());


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

        displayEventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        displayEventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));
        displayEventName.perform(click());

        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.clearText());
        eventName.perform(ViewActions.typeText("Test Edit Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Event")));

        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.clearText());
        eventAddress.perform(ViewActions.typeText("Test Edit Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Edit Address")));

        dateEditText.perform(click());
        Calendar currentDate = Calendar.getInstance();

// Calculate the next day's date
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        int year = currentDate.get(Calendar.YEAR);
        int monthOfYear = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, monthOfYear, dayOfMonth));

        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());


        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(17, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("17:00")));

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(19, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("19:00")));

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        displayEventName.check(ViewAssertions.doesNotExist());

        ViewInteraction nextDay = Espresso.onView(ViewMatchers.withId(R.id.nextDay));
        nextDay.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void addEventFailureTest() {
        ViewInteraction floatingButton = Espresso.onView(ViewMatchers.withId(R.id.floating_action_button));
        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());
        onView(ViewMatchers.withText("Cancel")).perform(ViewActions.click());

        String expectedToastMessageOne = "Please fill out all fields";

        onView(withText(expectedToastMessageOne))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

        floatingButton.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        floatingButton.perform(click());

        ViewInteraction eventName = Espresso.onView(ViewMatchers.withId(R.id.event_name));
        eventName.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventName.perform(ViewActions.typeText("Test Event"), ViewActions.closeSoftKeyboard());
        eventName.check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));

        ViewInteraction eventAddress = Espresso.onView(ViewMatchers.withId(R.id.event_address));
        eventAddress.check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        eventAddress.perform(ViewActions.typeText("Test Address"), ViewActions.closeSoftKeyboard());
        eventAddress.check(ViewAssertions.matches(ViewMatchers.withText("Test Address")));

        ViewInteraction dateEditText = Espresso.onView(ViewMatchers.withId(R.id.date_edit_text));
        dateEditText.perform(click());
        // Close the DatePicker
        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(17, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_start_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("17:00")));

        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(ViewActions.actionWithAssertions(PickerActions.setTime(16, 0)));
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.event_end_time))
                .check(ViewAssertions.matches(ViewMatchers.withText("16:00")));

        onView(ViewMatchers.withText("OK")).perform(ViewActions.click());
        onView(ViewMatchers.withText("Cancel")).perform(ViewActions.click());

        String expectedToastMessage = "Please input proper time";

        onView(withText(expectedToastMessage))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));

    }
}
