package com.example.routerider;

public interface FetchRoutesCallback {
    void onResponse(RouteItem routeItem);
    void onError();
}
