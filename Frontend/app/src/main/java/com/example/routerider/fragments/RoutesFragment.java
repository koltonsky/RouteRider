package com.example.routerider.fragments;

import static androidx.core.content.ContextCompat.startActivity;

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
import com.example.routerider.HelperFunc;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoutesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoutesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Date currentDay;
    private List<RouteItem> dayRoutes;

    private LinearLayout routesView;
    private Button getPreviousDay;
    private Button getNextDay;
    private TextView currentDayText;
    private DateFormat formatter;

    public RoutesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoutesFragment newInstance(String param1, String param2) {
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void fetchRoutes(View view, Date day) {
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

//        // Create a Handler to implement the timeout
//        Handler handler = new Handler();
//
//        // Define a timeout duration in milliseconds (2 seconds)
//        int timeoutDuration = 500;
//
//        // Start a timer when you make the API call
//        handler.postDelayed(() -> {
//            // This code will run after the timeout duration (2 seconds)
//            // You can cancel the API call and handle the timeout here
//            // apiCall.wait(); // Assuming there is a method to cancel the API call
//            displayRoutes(view,getContext());
//            // Handle the timeout, e.g., show an error message or perform any other action
//            // For example, you can display a Toast message:
//            getActivity().runOnUiThread(() -> {
//                Toast.makeText(getContext(), "API call timed out", Toast.LENGTH_SHORT).show();
//            });
//        }, timeoutDuration);

        apiCall.APICall("api/recommendation/routes/" + account.getEmail() + "/" + formatter.format(day), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) {
                // handler.removeCallbacksAndMessages(null);
                getActivity().runOnUiThread(() -> {
                    System.out.println("BODY: " + responseBody);
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray routes =  json.getJSONArray("routes");
                        System.out.println(routes);
                        dayRoutes = new ArrayList<>();
                        List<TransitItem> transitItemList = new ArrayList<>();
                        List<String> stepsList = new ArrayList<>();
                        for (int i = 0; i < routes.length(); i++) {
                            JSONObject item = (JSONObject) routes.get(i);
                            if ( item.has("_id")) {
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
                        dayRoutes.add(routeItem);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
            }
        });
    }

    private void displayRoutes(View view, Context context) {
        routesView = view.findViewById(R.id.routesView);
        routesView.removeAllViewsInLayout();
        LayoutInflater inflater = LayoutInflater.from(context);
        System.out.println("displaying routes");
        System.out.println(dayRoutes);
        if (dayRoutes == null || dayRoutes.isEmpty()){
            TextView emptyRoutes = new TextView(context);
            emptyRoutes.setText("There are no routes for this day");
            routesView.addView(emptyRoutes);
            return;
        }
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

        getPreviousDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, -1); // Subtract 1 day to get the previous day
            Date previousDay = calendar.getTime();
            changeDay(previousDay);
            fetchRoutes(view, previousDay);
            displayRoutes(view, getContext());
        });

        getNextDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, 1); // Add 1 day to get the next day
            Date nextDay = calendar.getTime();
            changeDay(nextDay);
            fetchRoutes(view, nextDay);
            displayRoutes(view, getContext());

        });

        fetchRoutes(view, new Date());
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