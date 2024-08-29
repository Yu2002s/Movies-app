package com.dongyu.movies.view.player;

/**
 * 视频显示缩放模式
 */
public enum VideoScaleMode {
    /**
     * 自适应
     */
    AUTO("自适应"),
    /**
     * 全屏铺满
     */
    FULL("铺满");

    private final String title;

    VideoScaleMode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static VideoScaleMode getModeForTitle(String title) {
        for (VideoScaleMode mode : values()) {
            if (mode.title.equals(title)) {
                return mode;
            }
        }
        return VideoScaleMode.AUTO;
    }
}
