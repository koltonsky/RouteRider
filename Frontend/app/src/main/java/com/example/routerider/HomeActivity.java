package com.example.routerider;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    private long backPressedTime;
    public static List<RouteItem> dayRoutes;
    public static List<RouteItem> weeklyRoutes;



    // YES CHATGPT
    public static void fetchRoutes(Date day, FetchRoutesCallback callback) {
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        apiCall.APICall("api/recommendation/routes/" + account.getEmail() + "/" + formatter.format(day), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) {
                // handler.removeCallbacksAndMessages(null);
                System.out.println("BODY ROUTES: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray routes =  json.getJSONArray("routes");
                    System.out.println(routes);
                    dayRoutes = new ArrayList<>();
                    List<TransitItem> transitItemList = new ArrayList<>();
                    List<String> stepsList = new ArrayList<>();
                    String destinationAddress = "";
                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject item = (JSONObject) routes.get(i);
                        if ( item.has("_id")) {
                            String id = item.getString("_id");
                            String type = item.getString("_type");
                            String leaveTime = item.getString("_leaveTime");
                            TransitItem transitItem = new TransitItem(id, type, leaveTime);
                            transitItemList.add(transitItem);
                        } else if (item.has("steps")){
                            JSONArray steps = item.getJSONArray("steps");
                            for (int j = 0; j < steps.length(); j++) {
                                String element = steps.getString(j);
                                stepsList.add(element);
                            }
                        }
                        else {
                            destinationAddress = item.getString("_destination");
                        }
                    }
                    RouteItem routeItem = new RouteItem(transitItemList, stepsList, destinationAddress);
                    callback.onResponse(routeItem);
                    // dayRoutes.add(routeItem);
                    //Log.d("DAY ROUTES", "ADDED DAY ROUTE");
                    //System.out.println(dayRoutes);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onError();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
                callback.onError();
            }
        });
    }


    public static String[] getWeekRange(Date inputDate) {
        String[] weekRange = new String[2];

        // Create a calendar instance and set it to the input date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);

        // Check if the input date is earlier than today
        Calendar todayCalendar = Calendar.getInstance();
        if (calendar.before(todayCalendar)) {
            // If so, set the input date to today
            calendar.setTime(new Date());
        }

        setToMinimumTime(calendar);

        // Format the start date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        weekRange[0] = dateFormat.format(calendar.getTime());

        // Set the calendar to the end of the week (Saturday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        setToMaximumTime(calendar);

        // Format the end date
        weekRange[1] = dateFormat.format(calendar.getTime());

        return weekRange;
    }



    public static void setToMinimumTime(Calendar calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        private static void setToMaximumTime(Calendar calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
        }

    public static void fetchWeeklyRoutes(Date day, FetchWeeklyRoutesCallback callback) {
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();
        String[] weekRange = getWeekRange(day);
        apiCall.APICall("api/recommendation/routes/" + account.getEmail() + "/" + weekRange[0] + "/" + weekRange[1], "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) {
                // handler.removeCallbacksAndMessages(null);
                System.out.println("BODY WEEKLY ROUTES: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray routes =  json.getJSONArray("weeklyRoutes");
                    System.out.println(routes);
                    weeklyRoutes = new ArrayList<>();
                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject dayRouteObject = routes.getJSONObject(i);
                        String routeDate = dayRouteObject.getString("date");
                        JSONArray dayRoute = dayRouteObject.getJSONArray("routes");
                        List<TransitItem> transitItemList = new ArrayList<>();
                        List<String> stepsList = new ArrayList<>();
                        String destinationAddress = "";
                        for (int j = 0; j < dayRoute.length(); j++) {
                            JSONObject item = (JSONObject) dayRoute.get(j);
                            if (item.has("_id")) {
                                String id = item.getString("_id");
                                String type = item.getString("_type");
                                String leaveTime = item.getString("_leaveTime");
                                TransitItem transitItem = new TransitItem(id, type, leaveTime);
                                transitItemList.add(transitItem);
                            } else if (item.has("steps")) {
                                JSONArray steps = item.getJSONArray("steps");
                                for (int k = 0; k < steps.length(); k++) {
                                    String element = steps.getString(k);
                                    stepsList.add(element);
                                }
                            } else {
                                destinationAddress = item.getString("_destination");
                            }
                        }
                        RouteItem routeItem = new RouteItem(routeDate, transitItemList, stepsList, destinationAddress);
                        weeklyRoutes.add(routeItem);
                    }
                    callback.onResponse(weeklyRoutes);
                    // dayRoutes.add(routeItem);
                    //Log.d("DAY ROUTES", "ADDED DAY ROUTE");
                    //System.out.println(dayRoutes);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onError();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
                callback.onError();
            }
        });
    }


    // NO CHATGPT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //FirebaseMessaging.getInstance().subscribeToTopic("RouteRider");

        // fetchRoutes(new Date());
        PushNotificationService pushNotificationService = new PushNotificationService();
        //pushNotificationService.initializeFirebase(getApplicationContext());

        // Retrieve the FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d("NOTIFICATION TAG", "SUCCESS: " + token);
                        pushNotificationService.sendRegistrationToServer(token);
                    } else {
                        // Handle the case where token retrieval fails
                        Log.d("NOTIFICATION TAG", "FAILED");
                    }
                });

        GoogleSignInAccount account = User.getCurrentAccount();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("email", account.getEmail());
        String requestJson = new Gson().toJson(requestMap);
        APICaller apiCall = new APICaller();
        apiCall.APICall("api/initReminders", requestJson, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
            @Override
            public void onResponse(String responseBody) {
                System.out.println("BODY: " + responseBody);
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error: " + errorMessage);
            }
        });

        // Set up the ViewPager with the sections adapter.
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter pageAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Should be kept empty
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Should be kept empty
            }
        });

        viewPager.setUserInputEnabled(false);
    }

    // NO CHATGPT
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}