package com.dongyu.movies.event;

/**
 * 线路改变时
 */
public interface OnRouteChangeListener {

    void onSelectionChanged(int routeId, int currentRoute, int currentSelection);

    void onNextSelection(int routeId, int currentRoute, int selection);
}
