package com.example.routerider.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import static com.example.routerider.HomeActivity.dayRoutes;
import static com.example.routerider.HomeActivity.fetchRoutes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.routerider.APICaller;
import com.example.routerider.FetchRoutesCallback;
import com.example.routerider.FriendsActivity;
import com.example.routerider.HelperFunc;
import com.example.routerider.HomeActivity;
import com.example.routerider.R;
import com.example.routerider.RouteItem;
import com.example.routerider.ScheduleItem;
import com.example.routerider.TransitItem;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class RoutesFragment extends Fragment {
    private Date currentDay;
    private List<RouteItem> dayRoutes;
    private LinearLayout routesView;
    private Button getPreviousDay;
    private Button getNextDay;
    private TextView currentDayText;
    private DateFormat formatter;

    private Button transitFriendButton;

    public RoutesFragment() {
        // Required empty public constructor
    }

//    private void fetchRoutes(View view, Date day) {
//        GoogleSignInAccount account = User.getCurrentAccount();
//        APICaller apiCall = new APICaller();
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//
////        // Create a Handler to implement the timeout
////        Handler handler = new Handler();
////
////        // Define a timeout duration in milliseconds (2 seconds)
////        int timeoutDuration = 500;
////
////        // Start a timer when you make the API call
////        handler.postDelayed(() -> {
////            // This code will run after the timeout duration (2 seconds)
////            // You can cancel the API call and handle the timeout here
////            // apiCall.wait(); // Assuming there is a method to cancel the API call
////            displayRoutes(view,getContext());
////            // Handle the timeout, e.g., show an error message or perform any other action
////            // For example, you can display a Toast message:
////            getActivity().runOnUiThread(() -> {
////                Toast.makeText(getContext(), "API call timed out", Toast.LENGTH_SHORT).show();
////            });
////        }, timeoutDuration);
//
//        apiCall.APICall("api/recommendation/routes/" + account.getEmail() + "/" + formatter.format(day), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
//            @Override
//            public void onResponse(final String responseBody) {
//                // handler.removeCallbacksAndMessages(null);
//                getActivity().runOnUiThread(() -> {
//                    System.out.println("BODY: " + responseBody);
//                    try {
//                        JSONObject json = new JSONObject(responseBody);
//                        JSONArray routes =  json.getJSONArray("routes");
//                        System.out.println(routes);
//                        dayRoutes = new ArrayList<>();
//                        List<TransitItem> transitItemList = new ArrayList<>();
//                        List<String> stepsList = new ArrayList<>();
//                        for (int i = 0; i < routes.length(); i++) {
//                            JSONObject item = (JSONObject) routes.get(i);
//                            if ( item.has("_id")) {
//                                String id = item.getString("_id");
//                                String type = item.getString("_type");
//                                String leaveTime = item.getString("_leaveTime");
//                                TransitItem transitItem = new TransitItem(id, type, leaveTime);
//                                transitItemList.add(transitItem);
//                            } else {
//                                JSONArray steps = item.getJSONArray("steps");
//                                for (int j = 0; j < steps.length(); j++) {
//                                    String element = steps.getString(j);
//                                    stepsList.add(element);
//                                }
//                            }
//                        }
//                        RouteItem routeItem = new RouteItem(transitItemList, stepsList, "0", "0");
//                        dayRoutes.add(routeItem);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                System.out.println("Error " + errorMessage);
//            }
//        });
//    }

    private void displayRoutes(View view, Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        System.out.println("displaying routes");
        System.out.println(dayRoutes);
        routesView = view.findViewById(R.id.routesView);
        transitFriendButton = view.findViewById(R.id.transitFriendButton);

        if (dayRoutes == null || dayRoutes.isEmpty()){
            System.out.println("ROUTE CALLS ##########");
            System.out.println(dayRoutes);
            TextView emptyRoutes = new TextView(context);
            emptyRoutes.setText("There are no routes for this day");
            routesView.addView(emptyRoutes);
            transitFriendButton.setEnabled(false);
            return;
        }
        transitFriendButton.setEnabled(true);
        for (RouteItem item: dayRoutes) {
            View singleRouteView  = inflater.inflate(R.layout.view_route, routesView, false);
            ImageButton expandButton = singleRouteView.findViewById(R.id.expandButton);
            LinearLayout hiddenView = singleRouteView.findViewById(R.id.hidden_view);
            CardView cardView = singleRouteView.findViewById(R.id.base_cardview);
            expandButton.setOnClickListener(v -> {
                if (hiddenView.getVisibility() == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.baseline_expand_more_24);
                }
                else {
                    TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                    hiddenView.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.baseline_expand_less_24);
                }
            });
            TextView leaveByTimeText = singleRouteView.findViewById(R.id.leaveByTime);
            leaveByTimeText.setText("Leave by " + item.getLeaveBy());
            LinearLayout transitIdsView = singleRouteView.findViewById(R.id.transitIds);
            for (TransitItem transitItem: item.getTransitItems()){
                View transitChipView;
                if (transitItem.getType().toLowerCase().equals("bus")){
                    transitChipView  = inflater.inflate(R.layout.bus_chip, transitIdsView, false);
                    TextView busIdText = transitChipView.findViewById(R.id.busId);
                    busIdText.setText(transitItem.getId());
                } else if (transitItem.getType().toLowerCase().equals("skytrain")){
                    transitChipView  = inflater.inflate(R.layout.train_chip, transitIdsView, false);
                    TextView busIdText = transitChipView.findViewById(R.id.trainId);
                    busIdText.setText(transitItem.getId());
                } else {
                    transitChipView  = inflater.inflate(R.layout.walk_chip, transitIdsView, false);
                    // TextView busIdText = transitChipView.findViewById(R.id.trainId);
                    // busIdText.setText(transitItem.getId());
                }
                transitIdsView.addView(transitChipView);
            }
            for (String step: item.getSteps()) {
                int index = item.getSteps().indexOf(step) + 1;
//                stepTextView  = inflater.inflate(R.layout.bus_chip, hiddenView, false);
                TextView stepText = new TextView(context);
                stepText.setText(index + ". " + step);
                int paddingInDp = 8; // You can adjust this value as needed
                int leftPaddingDp = 16;
                float scale = context.getResources().getDisplayMetrics().density;
                int paddingInPx = (int) (paddingInDp * scale + 0.5f);
                int leftPaddingPx = (int) (leftPaddingDp * scale + 0.5f);
                stepText.setPadding(leftPaddingPx, paddingInPx, paddingInPx, paddingInPx);
                hiddenView.addView(stepText);

            }
            routesView.addView(singleRouteView);
        }
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//        mockRoutes();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_routes, container, false);
        getPreviousDay = view.findViewById(R.id.previousDay);
        getPreviousDay.setEnabled(false);
        getNextDay = view.findViewById(R.id.nextDay);
        getNextDay.setEnabled(true);
        currentDay = new Date();
        formatter = new SimpleDateFormat("E, dd MMM");
        currentDayText = view.findViewById(R.id.currentDayText);
        currentDayText.setText(formatter.format(currentDay));

        transitFriendButton = view.findViewById(R.id.transitFriendButton);

        JSONArray friendsList = FriendsActivity.getFriendList();
        transitFriendButton.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setTitle("Friend List");

            String[] friendNames = {"Kolton (koltonluu@gmail.com)"};
            for (int i = 0; i < friendsList.length(); i++) {
                try {
                    JSONObject friend = friendsList.getJSONObject(i);

                    String email = friend.getString("email");
                    String name = friend.getString("name");
                    friendNames[i] = name + " (" + email + ")";

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Set the item list and a click listener
            alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
                String selectedFriend = friendNames[which];
                String selectedFriendEmail = "";

                int start = selectedFriend.indexOf('(');
                int end = selectedFriend.indexOf(')');

                if (start != -1 && end != -1 && start < end) {
                    selectedFriendEmail = selectedFriend.substring(start + 1, end);
                }
                APICaller apiCall = new APICaller();
                // Perform actions with the selected friend

                GoogleSignInAccount account = User.getCurrentAccount();

                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                apiCall.APICall("api/recommendation/routesWithFriends/" + account.getEmail() + "/" + selectedFriendEmail + "/" + formatter.format(currentDay), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
                    @Override
                    public void onResponse(String responseBody) {
                        try {
                            System.out.println("ROUTESWITHFRIENDS RESPONSE");
                            System.out.println("BODY: " + responseBody);
                            getActivity().runOnUiThread(() -> {
                                try {
                                    JSONObject json = new JSONObject(responseBody);
                                    JSONArray routes = json.getJSONArray("routes");
                                    System.out.println(routes);
                                    dayRoutes = new ArrayList<>();
                                    List<TransitItem> transitItemList = new ArrayList<>();
                                    List<String> stepsList = new ArrayList<>();
                                    for (int i = 0; i < routes.length(); i++) {
                                        JSONObject item = (JSONObject) routes.get(i);
                                        if (item.has("_id")) {
                                            String id = item.getString("_id");
                                            String type = item.getString("_type");
                                            String leaveTime = item.getString("_leaveTime");
                                            TransitItem transitItem = new TransitItem(id, type, leaveTime);
                                            transitItemList.add(transitItem);
                                        } else {
                                            JSONArray steps = item.getJSONArray("steps");
                                            for (int j = 0; j < steps.length(); j++) {
                                                String element = steps.getString(j);
                                                stepsList.add(element);
                                            }
                                        }
                                    }
                                    RouteItem routeItem = new RouteItem(transitItemList, stepsList, "0", "0");
                                    System.out.println("DAYROUTES WITH FRIEND");
                                    dayRoutes.add(routeItem);
                                    routesView = view.findViewById(R.id.routesView);
                                    routesView.removeAllViewsInLayout();
                                    TextView friendText = new TextView(getContext());
                                    friendText.setText("With " + selectedFriend);
                                    routesView.addView(friendText);
                                    displayRoutes(view, getContext());
                                } catch (JSONException e) {
                                    Toast errorToast = new Toast(getContext());
                                    errorToast.setText("Error finding a matching route:" + e.getMessage());
                                    errorToast.show();
                                    e.printStackTrace();
                                }
                            });
                        } catch (Exception e){
                            Toast errorToast = new Toast(getContext());
                            errorToast.setText("Error finding a matching route:" + e.getMessage());
                            errorToast.show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast errorToast = new Toast(getContext());
                        errorToast.setText("Error finding a matching route:" + errorMessage);
                        errorToast.show();
                        System.out.println("Error " + errorMessage);
                    }
                });
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        });


        fetchRoutes(new Date(), new FetchRoutesCallback() {
            @Override
            public void onResponse(RouteItem routeItem) {
                getActivity().runOnUiThread(() -> {
                    dayRoutes = new ArrayList<>();
                    dayRoutes.add(routeItem);
                    routesView = view.findViewById(R.id.routesView);
                    routesView.removeAllViewsInLayout();
                    displayRoutes(view,getContext());
                });
            }
            @Override
            public void onError() {
                getActivity().runOnUiThread(() -> {
                    TextView emptyRoutes = new TextView(getContext());
                    emptyRoutes.setText("There are no routes for this day");
                    transitFriendButton.setEnabled(false);
                    routesView = view.findViewById(R.id.routesView);
                    routesView.removeAllViewsInLayout();
                    routesView.addView(emptyRoutes);
                });
            }
        });

        getPreviousDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, -1); // Subtract 1 day to get the previous day
            Date previousDay = calendar.getTime();
            changeDay(previousDay);
            fetchRoutes(previousDay, new FetchRoutesCallback() {
                @Override
                public void onResponse(RouteItem routeItem) {
                    getActivity().runOnUiThread(() -> {
                        dayRoutes = new ArrayList<>();
                        dayRoutes.add(routeItem);
                        routesView = view.findViewById(R.id.routesView);
                        routesView.removeAllViewsInLayout();
                        displayRoutes(view, getContext());
                    });
                }
                @Override
                public void onError() {
                    getActivity().runOnUiThread(() -> {
                        TextView emptyRoutes = new TextView(getContext());
                        emptyRoutes.setText("There are no routes for this day");
                        transitFriendButton.setEnabled(false);
                        routesView = view.findViewById(R.id.routesView);
                        routesView.removeAllViewsInLayout();
                        routesView.addView(emptyRoutes);
                    });
                }
            });

        });

        getNextDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, 1); // Add 1 day to get the next day
            Date nextDay = calendar.getTime();
            changeDay(nextDay);
            fetchRoutes(nextDay, new FetchRoutesCallback() {
                @Override
                public void onResponse(RouteItem routeItem) {
                    getActivity().runOnUiThread(() -> {

                        dayRoutes = new ArrayList<>();
                        dayRoutes.add(routeItem);
                        routesView = view.findViewById(R.id.routesView);
                        routesView.removeAllViewsInLayout();
                        displayRoutes(view, getContext());
                    });
                }
                @Override
                public void onError() {
                    getActivity().runOnUiThread(() -> {
                        TextView emptyRoutes = new TextView(getContext());
                        emptyRoutes.setText("There are no routes for this day");
                        transitFriendButton.setEnabled(false);
                        routesView = view.findViewById(R.id.routesView);
                        routesView.removeAllViewsInLayout();
                        routesView.addView(emptyRoutes);
                    });
                }
            });

        });

        // fetchRoutes(new Date());
        displayRoutes(view, getContext());


        return view;
    }

    private void changeDay(Date day){
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.format(today).equals(sdf.format(day))) {
            getPreviousDay.setEnabled(false);
        } else {
            getPreviousDay.setEnabled(true);
        }
        currentDay = day;
        currentDayText.setText(formatter.format(day));
        // fetchRoutes(day);
        // displayRoutes();
    }
}