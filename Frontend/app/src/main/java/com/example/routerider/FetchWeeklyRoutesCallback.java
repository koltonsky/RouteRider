package com.example.routerider;

import java.util.List;

public interface FetchWeeklyRoutesCallback {
    void onResponse(List<RouteItem> weekRoutes);
    void onError();
}
