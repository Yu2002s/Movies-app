package com.dongyu.movies.view.player.base;

public interface PlayerTouchListener {

    /**
     * 是否可以触摸
     * @param downX 按下的X坐标
     * @param downY 按下的Y坐标
     * @return true:可以触摸，false:不可以触摸
     */
    boolean canTouch(float downX, float downY);

    void onDown(float downX, float downY);

    /**
     * 单击屏幕
     */
    void onSingleClick();

    /**
     * 双击屏幕
     */
    void onDoubleClick();

    void onStartLongClick();

    void onStopLongClick();

    void onHorizontalMove(float distanceX, boolean isForward);

    void onHorizontalMoveEnd(boolean isCancel);

    void onCancelHorizontalMove();

    void onLeftVerticalMove(float distanceY);

    void onLeftVerticalMoveEnd(boolean isCancel);

    void onRightVerticalMove(float distanceY);

    void onRightVerticalMoveEnd(boolean isCancel);

}
