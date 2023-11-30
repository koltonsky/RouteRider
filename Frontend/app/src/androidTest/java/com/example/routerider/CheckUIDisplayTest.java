package com.example.routerider;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

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
public class CheckUIDisplayTest {
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
    @Test
    public void a_checkScheduleTab() {
        loginToGoogle();
        ViewInteraction scheduleTab = onView(allOf(withContentDescription("Schedule"),
                withParent(withParent(withId(R.id.tab_layout))),
                isDisplayed()))
                .check(matches(isDisplayed()));
        scheduleTab.perform(click());

        onView(allOf(withId(R.id.schedule_title), withText("Schedule"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.previous_day), withText("Back"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next_day), withText("Next"))).check(matches(isDisplayed()));
        onView(withId(R.id.floating_action_button)).check(matches(isDisplayed()));
        onView(withId(R.id.current_day_text)).check(matches(isDisplayed()));
    }

    // NO CHATGPT
    @Test
    public void b_checkRouteTab() {
        loginToGoogle();
        ViewInteraction routeTab = onView(allOf(withContentDescription("Routes"),
                        withParent(withParent(withId(R.id.tab_layout))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
        routeTab.perform(click());

        onView(allOf(withId(R.id.route_title), withText("Routes"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.previous_day_route), withText("Back"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next_day_route), withText("Next"))).check(matches(isDisplayed()));
        onView(withId(R.id.current_day_text))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // onView(withId(R.id.transit_friend_button)).check(matches(isDisplayed()));
    }

    // NO CHATGPT
    @Test
    public void c_checkProfileTab() {
        loginToGoogle();
        ViewInteraction profileTab = onView(allOf(withContentDescription("Profile"),
                        withParent(withParent(withId(R.id.tab_layout))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
        profileTab.perform(click());

        onView(allOf(withId(R.id.logout_button), withText("LOGOUT"),
                withParent(withParent(isAssignableFrom(FrameLayout.class))),
                isDisplayed()));
        onView(withId(R.id.name_text)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_address))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.friend_list)).check(matches(isDisplayed()));
    }

    private void loginToGoogle() {
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
                Log.d("Account Error", "Failed to click account");
            }

            uiDevice.waitForIdle();
        } catch (Exception e) {
            Log.d("Continue Test", "Already Logged In");
        }
    }
}
