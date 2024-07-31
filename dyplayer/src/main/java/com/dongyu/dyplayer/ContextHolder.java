package com.dongyu.dyplayer;

import android.annotation.SuppressLint;
import android.content.Context;

public class ContextHolder {

  @SuppressLint("StaticFieldLeak")
  public static Context context;

  public static Context getContext() {
    return context;
  }

  public static void setContext(Context context) {
    ContextHolder.context = context;
  }
}
