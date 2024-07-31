package com.dongyu.movies.view.player.base;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

public class PlayerTouchHelp {

    private static final String TAG = "PlayerTouchHelp";

    private static final float ALLOW_MOVE_DISTANCE = 20;

    private static final int MAX_DOUBLE_TIME = 300;

    private static final int LONG_CLICK_TIME = 800;

    private static final int MOVE_CANCEL_REGION = 150;

    private float downX;
    private float downY;

    private float moveX;

    private float moveY;

    private float distanceX;

    private float distanceY;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable speedRunnable;

    private boolean isDoubleClick;

    private boolean isLongClick;

    private boolean isHorizontalMove;

    private boolean isLeftVerticalMove;

    private boolean isRightVerticalMove;

    private boolean isCancelHorizontalMove;

    private long currentClickTime = 0;

    private int playerHeight = 0;

    private int playerWidth = 0;

    private PlayerTouchListener listener;

    public void setPlayerTouchListener(PlayerTouchListener listener) {
        this.listener = listener;
    }

    public void setPlayerSize(int playerWidth, int playerHeight) {
        this.playerHeight = playerHeight;
        this.playerWidth = playerWidth;
    }

    public boolean bind(MotionEvent event) {
        if (listener == null)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                // 不能触摸的区域
                if (!listener.canTouch(downX, downY)) {
                    return false;
                }
                Log.d(TAG, "downY: " + downY);
                handleDownEvent();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                distanceX = moveX - downX;
                distanceY = moveY - downY;
                handleMoveEvent();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleUpEvent();
                break;
        }
        return true;
    }

    private void resetData() {
        distanceX = 0;
        distanceY = 0;
        moveY = 0;
        moveX = 0;
        isLeftVerticalMove = false;
        isRightVerticalMove = false;
        isHorizontalMove = false;
        isCancelHorizontalMove = false;
    }

    private void handleDownEvent() {
        isDoubleClick = isDoubleClick();
        if (isDoubleClick) {
            // 响应双击事件
            listener.onDoubleClick();
        } else {
            currentClickTime = System.currentTimeMillis();
        }
        if (speedRunnable == null) {
            speedRunnable = new SpeedRunnable();
        }
        mHandler.postDelayed(speedRunnable, LONG_CLICK_TIME);

        resetData();

        listener.onDown(downX, downY);
    }

    private void handleMoveEvent() {
        // 过滤双击和长按事件
        if (isDoubleClick || isLongClick) {
            return;
        }

        float moveDistanceX = Math.abs(distanceX);
        float moveDistanceY = Math.abs(distanceY);

        if (!isHorizontalMove && moveDistanceY > ALLOW_MOVE_DISTANCE) {
            if (downX <= playerWidth / 2f) {
                isLeftVerticalMove = true;
                listener.onLeftVerticalMove(distanceY);
            } else {
                isRightVerticalMove = true;
                listener.onRightVerticalMove(distanceY);
            }
        }

        if (moveDistanceX > ALLOW_MOVE_DISTANCE && !isLeftVerticalMove && !isRightVerticalMove) {
            isHorizontalMove = true;
            if (!isHorizontalMoveRegin()) {
                if (!isCancelHorizontalMove) {
                    isCancelHorizontalMove = true;
                    listener.onCancelHorizontalMove();
                }
                return;
            } else if (isCancelHorizontalMove) {
                isCancelHorizontalMove = false;
            }
            listener.onHorizontalMove(distanceX, downX < moveX);
        }
    }

    private void handleUpEvent() {
        if (isAllowSingleClick()) {
            mHandler.postDelayed(new ClickRunnable(), MAX_DOUBLE_TIME);
        }
        if (speedRunnable != null) {
            mHandler.removeCallbacks(speedRunnable);
            speedRunnable = null;
            if (isLongClick) {
                // 删除已取消的队列
                mHandler.removeCallbacksAndMessages(null);
                isLongClick = false;
                listener.onStopLongClick();
            }
        }

        if (isHorizontalMove) {
            listener.onHorizontalMoveEnd(isCancelHorizontalMove);
        } else if (isLeftVerticalMove) {
            listener.onLeftVerticalMoveEnd(isCancelHorizontalMove);
        } else if (isRightVerticalMove) {
            listener.onRightVerticalMoveEnd(isCancelHorizontalMove);
        }
    }

    private class ClickRunnable implements Runnable {

        @Override
        public void run() {
            if (isAllowSingleClick()) {
                listener.onSingleClick();
            }
        }
    }

    private class SpeedRunnable implements Runnable {

        @Override
        public void run() {
            // 响应移动事件时，不执行长按事件
            if (isMove()) {
                return;
            }
            isLongClick = true;
            listener.onStartLongClick();
        }
    }

    private boolean isAllowSingleClick() {
        return !isDoubleClick && Math.abs(distanceX) < ALLOW_MOVE_DISTANCE
                && Math.abs(distanceY) < ALLOW_MOVE_DISTANCE && !isMove();
    }

    private boolean isDoubleClick() {
        return System.currentTimeMillis() - currentClickTime < MAX_DOUBLE_TIME;
    }

    private boolean isMove() {
        return isHorizontalMove || isLeftVerticalMove || isRightVerticalMove;
    }

    private boolean isHorizontalMoveRegin() {
        return moveX > MOVE_CANCEL_REGION && moveX < playerWidth - MOVE_CANCEL_REGION;
    }

    /**
     * 销毁
     */
    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

}
