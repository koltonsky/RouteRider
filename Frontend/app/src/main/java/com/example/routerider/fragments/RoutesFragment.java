package com.example.routerider.fragments;

import static com.example.routerider.HomeActivity.fetchRoutes;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
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

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.routerider.APICaller;
import com.example.routerider.FetchRoutesCallback;
import com.example.routerider.R;
import com.example.routerider.RouteItem;
import com.example.routerider.TransitItem;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@FunctionalInterface
interface FetchFriendRoutesCallback {
    void execute();
}

public class RoutesFragment extends Fragment {
    private Date currentDay;
    private List<RouteItem> dayRoutes;
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
    private void displayRoutes(View view, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        routesView = view.findViewById(R.id.routes_view);
        transitFriendButton = view.findViewById(R.id.transit_friend_button);

        if (dayRoutes == null || dayRoutes.isEmpty()){
            TextView emptyRoutes = new TextView(context);
            emptyRoutes.setText("There are no routes for this day");
            routesView.addView(emptyRoutes);
            transitFriendButton.setEnabled(false);
            return;
        }

        transitFriendButton.setEnabled(true);
        for (RouteItem item: dayRoutes) {
            View singleRouteView  = inflater.inflate(R.layout.view_route, routesView, false);
            ImageButton expandButton = singleRouteView.findViewById(R.id.expand_button);
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

            TextView leaveByTimeText = singleRouteView.findViewById(R.id.leave_by_time);
            leaveByTimeText.setText("Leave by " + item.getLeaveBy());

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
            }
            for (String step: item.getSteps()) {
                int index = item.getSteps().indexOf(step) + 1;
                TextView stepText = new TextView(context);
                stepText.setText(index + ". " + step);
                int paddingInDp = 8;
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

    public FetchRoutesCallback createFetchRoutesCallback(View view) {
        return new FetchRoutesCallback() {
            @Override
            public void onResponse(RouteItem routeItem) {
                getActivity().runOnUiThread(() -> {
                    dayRoutes = new ArrayList<>();
                    dayRoutes.add(routeItem);
                    routesView = view.findViewById(R.id.routes_view);
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
                    routesView = view.findViewById(R.id.routes_view);
                    routesView.removeAllViewsInLayout();
                    routesView.addView(emptyRoutes);
                });
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDay = new Date();
        dayTextFormatter = new SimpleDateFormat("E, dd MMM");
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

    private void fetchFriendRoutes(String email, FetchFriendRoutesCallback callback) {
        DateFormat apiDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        apiCall.APICall("api/recommendation/routesWithFriends/" + account.getEmail() + "/" + email + "/" + apiDateFormatter.format(currentDay), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
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
                    RouteItem routeItem = new RouteItem(transitItemList, stepsList);
                    System.out.println("DAYROUTES WITH FRIEND");
                    dayRoutes.add(routeItem);
                    getActivity().runOnUiThread(() -> {
                        callback.execute();
                    });
                } catch (Exception e){
                    showFriendRouteError();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showFriendRouteError();
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
        previousDayButton = view.findViewById(R.id.previous_day);
        previousDayButton.setEnabled(false);
        Button nextDayButton = view.findViewById(R.id.next_day);
        nextDayButton.setEnabled(true);
        currentDayText = view.findViewById(R.id.current_day_text);
        currentDayText.setText(dayTextFormatter.format(currentDay));
        transitFriendButton = view.findViewById(R.id.transit_friend_button);

        transitFriendButton.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setTitle("Friend List");
            friendNames = fetchFriendsList();
            alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
                String selectedFriend = friendNames[which];
                fetchFriendRoutes(parseEmail(selectedFriend), () -> {
                    routesView = view.findViewById(R.id.routes_view);
                    routesView.removeAllViewsInLayout();
                    TextView friendText = new TextView(getContext());
                    friendText.setText("With " + selectedFriend);
                    routesView.addView(friendText);
                    displayRoutes(view, getContext());
                });
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        });

        FetchRoutesCallback fetchRoutesCallback = createFetchRoutesCallback(view);

        previousDayButton.setOnClickListener(v -> {
            getDay(-1, fetchRoutesCallback);
        });

        nextDayButton.setOnClickListener(v -> {
            getDay(1, fetchRoutesCallback);
        });

        fetchRoutes(new Date(), fetchRoutesCallback);
        return view;
    }

    private void getDay(int gap, FetchRoutesCallback callback) {
        java.util.Calendar calendar =  java.util.Calendar.getInstance();
        calendar.setTime(currentDay);
        calendar.add( java.util.Calendar.DAY_OF_YEAR, gap);
        Date newDay = calendar.getTime();
        updateDateDisplay(newDay);
        fetchRoutes(newDay, callback);
    }

    private void showFriendRouteError() {
        Looper.prepare();
        Toast errorToast = Toast.makeText(getContext(), "Error finding a matching route.", Toast.LENGTH_SHORT);
        errorToast.show();
    }

    // NO CHATGPT
    private void updateDateDisplay(Date day){
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.format(today).equals(sdf.format(day))) {
            previousDayButton.setEnabled(false);
        } else {
            previousDayButton.setEnabled(true);
        }
        currentDay = day;
        currentDayText.setText(dayTextFormatter.format(day));
    }
}