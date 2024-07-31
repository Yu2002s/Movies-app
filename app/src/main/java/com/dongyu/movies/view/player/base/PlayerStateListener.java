package com.dongyu.movies.view.player.base;

public interface PlayerStateListener {

    default void onPlayerCreated(BasePlayer player) {}

    default void onVideoPrepared(BasePlayer player) {}

    default void onScreenChanged(boolean isFullScreen) {}

    default void onProgressChanged(long currentProgress) {}

    default void onPlayStateChanged(int newState) {}

    default void onShowController() {}

    default void onHideController() {}

    default void onLockStateChanged(boolean isLockScreen) {}
}
