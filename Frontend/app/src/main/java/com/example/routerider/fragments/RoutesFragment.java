package com.example.routerider.fragments;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static com.example.routerider.FriendsActivity.sendFriendRequest;
import static com.example.routerider.HomeActivity.fetchRoutes;
import static com.example.routerider.HomeActivity.fetchWeeklyRoutes;
import static com.example.routerider.HomeActivity.setToMinimumTime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.routerider.APICaller;
import com.example.routerider.FetchRoutesCallback;
import com.example.routerider.FetchWeeklyRoutesCallback;
import com.example.routerider.FriendRequestErrorCallback;
import com.example.routerider.FriendsActivity;
import com.example.routerider.R;
import com.example.routerider.RouteItem;
import com.example.routerider.TransitItem;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
interface FetchFriendRoutesCallback {
    void execute(RouteItem routeItem) throws ParseException;
}

public class RoutesFragment extends Fragment {
    private Date currentDay;
    private List<RouteItem> dayRoutes;
    private List<RouteItem> weeklyRoutes;
    private LinearLayout routesView;
    private Button previousDayButton;
    private TextView currentDayText;
    private DateFormat dayTextFormatter;
    private Button transitFriendButton;
    private GoogleSignInAccount account;
    private APICaller apiCall;

    public static String[] friendNames;
    public static String[] matchingCommuters;



    // YES CHATGPT
//    private void displayRoutes(View view, Context context, String friendIndicator) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        routesView = view.findViewById(R.id.routes_view);
//        transitFriendButton = view.findViewById(R.id.transit_friend_button);
//
//        if (dayRoutes == null || dayRoutes.isEmpty()){
//            TextView emptyRoutes = new TextView(context);
//            emptyRoutes.setText("There are no routes for this day");
//            routesView.addView(emptyRoutes);
//            transitFriendButton.setEnabled(false);
//            return;
//        }
//
//        transitFriendButton.setEnabled(true);
//        for (RouteItem item: dayRoutes) {
//            View singleRouteView  = inflater.inflate(R.layout.view_route, routesView, false);
//            ImageButton expandButton = singleRouteView.findViewById(R.id.expand_button);
//            LinearLayout hiddenView = singleRouteView.findViewById(R.id.hidden_view);
//            CardView cardView = singleRouteView.findViewById(R.id.base_cardview);
//
//            expandButton.setOnClickListener(v -> {
//                if (hiddenView.getVisibility() == View.VISIBLE) {
//                    TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
//                    hiddenView.setVisibility(View.GONE);
//                    expandButton.setImageResource(R.drawable.baseline_expand_more_24);
//                }
//                else {
//                    TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
//                    hiddenView.setVisibility(View.VISIBLE);
//                    expandButton.setImageResource(R.drawable.baseline_expand_less_24);
//                }
//            });
//
//            TextView leaveByTimeText = singleRouteView.findViewById(R.id.leave_by_time);
//            leaveByTimeText.setText("Leave by " + item.getLeaveBy());
//
//            ImageButton recMapsButton = singleRouteView.findViewById(R.id.maps_button);
//            recMapsButton.setOnClickListener(v2 -> {
//                Intent intent = new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("google.navigation:q=" + item.getDestination()));
//                view.getContext().startActivity(intent);
//            });
//
//            LinearLayout transitIdsView = singleRouteView.findViewById(R.id.transit_ids);
//            for (TransitItem transitItem: item.getTransitItems()){
//                View transitChipView;
//                if (transitItem.getType().equalsIgnoreCase("bus")){
//                    transitChipView  = inflater.inflate(R.layout.bus_chip, transitIdsView, false);
//                    TextView busIdText = transitChipView.findViewById(R.id.bus_id);
//                    busIdText.setText(transitItem.getId());
//                } else if (transitItem.getType().equalsIgnoreCase("skytrain")){
//                    transitChipView  = inflater.inflate(R.layout.train_chip, transitIdsView, false);
//                    TextView busIdText = transitChipView.findViewById(R.id.train_id);
//                    busIdText.setText(transitItem.getId());
//                } else {
//                    transitChipView  = inflater.inflate(R.layout.walk_chip, transitIdsView, false);
//                }
//                transitIdsView.addView(transitChipView);
//                if (item.getTransitItems().indexOf(transitItem) != item.getTransitItems().size() - 1){
//                    TextView bulletTextView = new TextView(getContext());
//                    bulletTextView.setText(" • ");
//                    bulletTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                    transitIdsView.addView(bulletTextView);
//                }
//            }
//            for (String step: item.getSteps()) {
//                int index = item.getSteps().indexOf(step) + 1;
//                TextView stepText = new TextView(context);
//                stepText.setText(index + ". " + step + " ↗");
//                int paddingInDp = 8;
//                int leftPaddingDp = 16;
//                float scale = context.getResources().getDisplayMetrics().density;
//                int paddingInPx = (int) (paddingInDp * scale + 0.5f);
//                int leftPaddingPx = (int) (leftPaddingDp * scale + 0.5f);
//                stepText.setPadding(leftPaddingPx, paddingInPx, paddingInPx, paddingInPx);
//                stepText.setOnClickListener(v -> {
//                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + extractLocation(step));
//                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                    mapIntent.setPackage("com.google.android.apps.maps");
//                    view.getContext().startActivity(mapIntent);
//                }
//                );
//                hiddenView.addView(stepText);
//
//            }
//            routesView.addView(singleRouteView);
//            Button transitFriendButton = singleRouteView.findViewById(R.id.friend_button);
//            transitFriendButton.setOnClickListener(v -> {
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
//                alertDialogBuilder.setTitle("Friend List");
//                friendNames = fetchFriendsList();
//                alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
//                    String selectedFriend = friendNames[which];
//                    fetchFriendRoutes("", parseEmail(selectedFriend), () -> {
//                        routesView = view.findViewById(R.id.routes_view);
//                        routesView.removeAllViewsInLayout();
//                        displayRoutes(view, getContext(), "With " + selectedFriend);
//                    });
//                });
//
//                alertDialogBuilder.setPositiveButton("Find matching commuters", (dialog, which) -> {
//                    // Add logic to handle the button click
//                    // You can perform any actions you need when the button is clicked
//                    // For example, start the process of finding matching commuters
//                    System.out.println("MATCHING COMMUTERS");
//                });
//
//                // Create and show the AlertDialog
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                alertDialog.show();
//
//            });
//            if (!friendIndicator.equals("")) {
//                TextView friendText = singleRouteView.findViewById(R.id.friend_indicator);
//                friendText.setText(friendIndicator);
//                friendText.setVisibility(View.VISIBLE);
//                transitFriendButton.setVisibility(View.GONE);
//            }
//        }
//    }

    public static String extractLocation(String direction) {
        // Define the pattern for the location information
        Pattern pattern = Pattern.compile("Walk to (.+)|Bus towards (.+)|Subway towards (.+)|(.+)");

        // Match the pattern against the input direction
        Matcher matcher = pattern.matcher(direction);

        // Find the location information
        if (matcher.find()) {
            // Choose the group that has a non-null match (non-empty location)
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return matcher.group(i).trim();
                }
            }
        }

        // Return null if no location information is found
        return null;
    }

    public FetchWeeklyRoutesCallback createFetchWeeklyRoutesCallback(View view) {
        return new FetchWeeklyRoutesCallback() {
            @Override
            public void onResponse(List<RouteItem> routes) {
                getActivity().runOnUiThread(() -> {
                    routesView = view.findViewById(R.id.routes_view);
                    routesView.removeAllViewsInLayout();
                    try {
                        displayWeeklyRoutes(routes, view, getContext());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void onError() {
                getActivity().runOnUiThread(() -> {
                    TextView emptyRoutes = new TextView(getContext());
                    emptyRoutes.setText("There are no routes for this week.");
                    emptyRoutes.setGravity(Gravity.CENTER);
                    routesView = view.findViewById(R.id.routes_view);
                    routesView.removeAllViewsInLayout();
                    routesView.addView(emptyRoutes);
                });
            }
        };
    }

    private String[] fetchMatchingCommuters() {
        List<String> friendArray  = new ArrayList<>();
        apiCall.APICall("api/findMatchingUsers/" + account.getEmail(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) throws JSONException {
                System.out.println("BODY: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);

                    JSONArray result = json.getJSONArray("matchingUsers");
                    for (int i = 0; i<result.length(); i++){
                        friendArray.add((String) result.get(i));
                    }

                    // Check if the "friendsWithNames" and "friendRequestsWithNames" keys exist in the JSON
                    if (json.has("friendsWithNames") && json.has("friendRequestsWithNames")) {
                        ProfileFragment.friendList = json.getJSONArray("friendsWithNames");
                        ProfileFragment.friendRequestList = json.getJSONArray("friendRequestsWithNames");

                        // Check if the arrays are empty
                        if (ProfileFragment.friendList.length() > 0) {
                            System.out.println(ProfileFragment.friendList);
                        } else {
                            System.out.println("Friend list is empty.");
                        }

                        if (ProfileFragment.friendRequestList.length() > 0) {
                            System.out.println(ProfileFragment.friendRequestList);
                        } else {
                            System.out.println("Friend request list is empty.");
                        }

                    } else {
                        System.out.println("The JSON object doesn't contain the expected keys.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        Toast errorToast = Toast.makeText(getContext(), "No matching commuters found.", Toast.LENGTH_SHORT);
                        errorToast.show();
                    });

                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
                getActivity().runOnUiThread(() -> {
                    Toast errorToast = Toast.makeText(getContext(), "No matching commuters found.", Toast.LENGTH_SHORT);
                    errorToast.show();
                });
            }
        });
//        for (int i = 0; i < ProfileFragment.friendList.length(); i++) {
//            try {
//                JSONObject friend = ProfileFragment.friendList.getJSONObject(i);
//
//                String email = friend.getString("email");
//                String name = friend.getString("name");
//                friendArray.add(name + " (" + email + ")");
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        return friendArray.toArray(new String[friendArray.size()]);
    }

    private void displayWeeklyRoutes(List<RouteItem> routes, View view, Context context) throws ParseException {
        LayoutInflater inflater = LayoutInflater.from(context);
        routesView = view.findViewById(R.id.routes_view);

        if (routes == null || routes.isEmpty()){
            TextView emptyRoutes = new TextView(context);
            emptyRoutes.setText("There are no routes for this week");
            routesView.addView(emptyRoutes);
            // transitFriendButton.setEnabled(false);
            return;
        }

        // transitFriendButton.setEnabled(true);
        for (RouteItem item: routes) {
            View singleRouteView  = inflater.inflate(R.layout.view_route, routesView, false);
            ImageButton expandButton = singleRouteView.findViewById(R.id.expand_button);
            LinearLayout hiddenView = singleRouteView.findViewById(R.id.hidden_view);
            CardView cardView = singleRouteView.findViewById(R.id.base_cardview);
            TextView dateText = singleRouteView.findViewById(R.id.route_date);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateTextFormat = new SimpleDateFormat("EEEE, MMMM dd");
            dateText.setText(dateTextFormat.format(sdf.parse(item.getDate())));
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

            TextView leaveByTimeText = singleRouteView.findViewById(R.id.leave_by_time);
            leaveByTimeText.setText("Leave by " + item.getLeaveBy());

            ImageButton recMapsButton = singleRouteView.findViewById(R.id.maps_button);
            recMapsButton.setOnClickListener(v2 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + item.getDestination()));
                view.getContext().startActivity(intent);
            });

            LinearLayout transitIdsView = singleRouteView.findViewById(R.id.transit_ids);
            for (TransitItem transitItem: item.getTransitItems()){
                View transitChipView;
                if (transitItem.getType().equalsIgnoreCase("bus")){
                    transitChipView  = inflater.inflate(R.layout.bus_chip, transitIdsView, false);
                    TextView busIdText = transitChipView.findViewById(R.id.bus_id);
                    busIdText.setText(transitItem.getId());
                } else if (transitItem.getType().equalsIgnoreCase("skytrain")){
                    transitChipView  = inflater.inflate(R.layout.train_chip, transitIdsView, false);
                    TextView busIdText = transitChipView.findViewById(R.id.train_id);
                    busIdText.setText(transitItem.getId());
                } else {
                    transitChipView  = inflater.inflate(R.layout.walk_chip, transitIdsView, false);
                }
                transitIdsView.addView(transitChipView);
                if (item.getTransitItems().indexOf(transitItem) != item.getTransitItems().size() - 1){
                    TextView bulletTextView = new TextView(getContext());
                    bulletTextView.setText(" • ");
                    bulletTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    transitIdsView.addView(bulletTextView);
                }
            }
            for (String step: item.getSteps()) {
                int index = item.getSteps().indexOf(step) + 1;
                TextView stepText = new TextView(context);
                stepText.setText(index + ". " + step + " ↗");
                int paddingInDp = 8;
                int leftPaddingDp = 16;
                float scale = context.getResources().getDisplayMetrics().density;
                int paddingInPx = (int) (paddingInDp * scale + 0.5f);
                int leftPaddingPx = (int) (leftPaddingDp * scale + 0.5f);
                stepText.setPadding(leftPaddingPx, paddingInPx, paddingInPx, paddingInPx);
                stepText.setOnClickListener(v -> {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + extractLocation(step));
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            view.getContext().startActivity(mapIntent);
                        }
                );
                hiddenView.addView(stepText);

            }
            routesView.addView(singleRouteView);
            Button transitFriendButton = singleRouteView.findViewById(R.id.friend_button);
            transitFriendButton.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
                alertDialogBuilder.setTitle("Friend List");
                friendNames = fetchFriendsList();
                alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
                    String selectedFriend = friendNames[which];
                    fetchFriendRoutes(item.getDate(), parseEmail(selectedFriend), (RouteItem newRouteItem) -> {
                        routesView = view.findViewById(R.id.routes_view);
                        routesView.removeAllViewsInLayout();
                        item.setFriend(selectedFriend, newRouteItem.getTransitItems(), newRouteItem.getSteps());
                        displayWeeklyRoutes(routes, view, context);
                    });
                });

                alertDialogBuilder.setPositiveButton("Find matching commuters", (dialog, which) -> {
                    System.out.println("MATCHING COMMUTERS");
                    AlertDialog.Builder otherAlertDialogBuilder = new AlertDialog.Builder(requireContext());
                    otherAlertDialogBuilder.setTitle("Matching Commuters");
                    String[] matchingCommuterNames = fetchMatchingCommuters();
                    otherAlertDialogBuilder.setItems(matchingCommuterNames, (dialog2, which2) -> {
                        String selectedFriend = matchingCommuterNames[which2];
                        sendFriendRequest(selectedFriend, (String message) -> {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        });
                    });
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            });
            if (!item.getFriendEmail().equals("")) {
                TextView friendText = singleRouteView.findViewById(R.id.friend_indicator);
                friendText.setText("With " + item.getFriendEmail());
                friendText.setVisibility(View.VISIBLE);
                transitFriendButton.setVisibility(View.GONE);
            }
        }
    }

//    public FetchRoutesCallback createFetchRoutesCallback(View view) {
//        return new FetchRoutesCallback() {
//            @Override
//            public void onResponse(RouteItem routeItem) {
//                getActivity().runOnUiThread(() -> {
//                    dayRoutes = new ArrayList<>();
//                    dayRoutes.add(routeItem);
//                    routesView = view.findViewById(R.id.routes_view);
//                    routesView.removeAllViewsInLayout();
//                    displayRoutes(view, getContext(),"");
//                });
//            }
//
//            @Override
//            public void onError() {
//                getActivity().runOnUiThread(() -> {
//                    TextView emptyRoutes = new TextView(getContext());
//                    emptyRoutes.setText("There are no routes for this day");
//                    transitFriendButton.setEnabled(false);
//                    routesView = view.findViewById(R.id.routes_view);
//                    routesView.removeAllViewsInLayout();
//                    routesView.addView(emptyRoutes);
//                });
//            }
//        };
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // currentDay = new Date();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Set the day of the week to Sunday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Set the time to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Print the result
        currentDay = calendar.getTime();

        dayTextFormatter = new SimpleDateFormat("MMM dd");
        account = User.getCurrentAccount();
        apiCall = new APICaller();
    }

    public String[] fetchFriendsList() {
        apiCall.APICall("api/userlist/" + account.getEmail() + "/friends", "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) throws JSONException {
                System.out.println("BODY: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);

                    // Check if the "friendsWithNames" and "friendRequestsWithNames" keys exist in the JSON
                    if (json.has("friendsWithNames") && json.has("friendRequestsWithNames")) {
                        ProfileFragment.friendList = json.getJSONArray("friendsWithNames");
                        ProfileFragment.friendRequestList = json.getJSONArray("friendRequestsWithNames");

                        // Check if the arrays are empty
                        if (ProfileFragment.friendList.length() > 0) {
                            System.out.println(ProfileFragment.friendList);
                        } else {
                            System.out.println("Friend list is empty.");
                        }

                        if (ProfileFragment.friendRequestList.length() > 0) {
                            System.out.println(ProfileFragment.friendRequestList);
                        } else {
                            System.out.println("Friend request list is empty.");
                        }

                    } else {
                        System.out.println("The JSON object doesn't contain the expected keys.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
            }
        });
        List<String> friendArray  = new ArrayList<>();
        for (int i = 0; i < ProfileFragment.friendList.length(); i++) {
            try {
                JSONObject friend = ProfileFragment.friendList.getJSONObject(i);

                String email = friend.getString("email");
                String name = friend.getString("name");
                friendArray.add(name + " (" + email + ")");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return friendArray.toArray(new String[friendArray.size()]);
    }

    private void fetchFriendRoutes(String date, String email, FetchFriendRoutesCallback callback) {
        DateFormat apiDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String apiDate = apiDateFormatter.format(currentDay);
        if (date != ""){
            apiDate = date;
        }
        apiCall.APICall("api/recommendation/routesWithFriends/" + account.getEmail() + "/" + email + "/" + apiDate, "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(String responseBody) {
                try {
                    System.out.println("ROUTESWITHFRIENDS RESPONSE");
                    System.out.println("BODY: " + responseBody);
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray routes = json.getJSONArray("routes");
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
                    System.out.println("DAYROUTES WITH FRIEND");
                    // dayRoutes.add(routeItem);
                    getActivity().runOnUiThread(() -> {
                        try {
                            callback.execute(routeItem);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e){
                    getActivity().runOnUiThread(() -> {
                        showFriendRouteError();
                    });
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                getActivity().runOnUiThread(() -> {
                    showFriendRouteError();
                });
                System.out.println("Error " + errorMessage);
            }
        });
    }

    private String parseEmail(String selection) {
        String selectedFriendEmail = "";

        int start = selection.indexOf('(');
        int end = selection.indexOf(')');

        if (start != -1 && end != -1 && start < end) {
            selectedFriendEmail = selection.substring(start + 1, end);
        }
        return selectedFriendEmail;
    }

    // YES CHATGPT
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_routes, container, false);
        previousDayButton = view.findViewById(R.id.previous_day_route);
        previousDayButton.setEnabled(false);
        Button nextDayButton = view.findViewById(R.id.next_day_route);
        nextDayButton.setEnabled(true);
        currentDayText = view.findViewById(R.id.current_day_text_route);
        currentDayText.setText("Week of "+ dayTextFormatter.format(currentDay));

//        transitFriendButton.setOnClickListener(v -> {
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
//            alertDialogBuilder.setTitle("Friend List");
//            friendNames = fetchFriendsList();
//            alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
//                String selectedFriend = friendNames[which];
//                fetchFriendRoutes("", parseEmail(selectedFriend), () -> {
//                    routesView = view.findViewById(R.id.routes_view);
//                    routesView.removeAllViewsInLayout();
//                    displayRoutes(view, getContext(), "With " + selectedFriend);
//
//                });
//            });
//
//            alertDialogBuilder.setPositiveButton("Find matching commuters", (dialog, which) -> {
//                // Add logic to handle the button click
//                // You can perform any actions you need when the button is clicked
//                // For example, start the process of finding matching commuters
//                System.out.println("MATCHING COMMUTERS");
//            });
//
//            // Create and show the AlertDialog
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
//
//        });

        FetchWeeklyRoutesCallback fetchWeeklyRoutesCallback = createFetchWeeklyRoutesCallback(view);

        previousDayButton.setOnClickListener(v -> {
            getDay(-1, fetchWeeklyRoutesCallback);
        });

        nextDayButton.setOnClickListener(v -> {
            getDay(1, fetchWeeklyRoutesCallback);
        });

        fetchWeeklyRoutes(new Date(), fetchWeeklyRoutesCallback);
        return view;
    }

    private void getDay(int gap, FetchWeeklyRoutesCallback callback) {
        java.util.Calendar calendar =  java.util.Calendar.getInstance();
        calendar.setTime(currentDay);
        calendar.add(Calendar.WEEK_OF_YEAR, gap);
        Date newDay = calendar.getTime();
        updateDateDisplay(newDay);
        fetchWeeklyRoutes(newDay, callback);
    }

    private void showFriendRouteError() {
        System.out.println("SHOW FRIEND ROUTE ERROR CALLED");
        Toast errorToast = Toast.makeText(getContext(), "Error finding a matching route.", Toast.LENGTH_SHORT);
        errorToast.show();
    }

    // NO CHATGPT
//    private void updateDateDisplay(Date day){
//        Date today = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        if (sdf.format(today).equals(sdf.format(day))) {
//            previousDayButton.setEnabled(false);
//        } else {
//            previousDayButton.setEnabled(true);
//        }
//        currentDay = day;
//        currentDayText.setText(dayTextFormatter.format(day));
//    }
    private void updateDateDisplay(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);

        // Check if the input date is not already the start of the week (Sunday)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            // If not, set the calendar to the start of the week (Sunday)
            setToMinimumTime(calendar);
        }

        // Get the first day of the week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        setToMinimumTime(calendar);

        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (today.after(day) || sdf.format(today).equals(sdf.format(day))) {
            previousDayButton.setEnabled(false);
        } else {
            previousDayButton.setEnabled(true);
        }

        currentDay = day;

        // Format the date as "Week of MM dd"
        SimpleDateFormat weekFormat = new SimpleDateFormat("MMM dd");
        currentDayText.setText("Week of " + weekFormat.format(calendar.getTime()));
    }
}