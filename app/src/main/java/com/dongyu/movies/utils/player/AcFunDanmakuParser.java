package com.dongyu.movies.utils.player;

import org.json.JSONArray;

import android.graphics.Color;


import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

public class AcFunDanmakuParser extends BaseDanmakuParser {

    @Override
    public Danmakus parse() {
        if (mDataSource != null && mDataSource instanceof JSONSource) {
            JSONSource jsonSource = (JSONSource) mDataSource;
            return doParse(jsonSource.data());
        }
        return new Danmakus();
    }

    /**
     * @param danmakuListData 弹幕数据
     *                        传入的数组内包含普通弹幕，会员弹幕，锁定弹幕。
     * @return 转换后的Danmakus
     */
    private Danmakus doParse(JSONArray danmakuListData) {
        Danmakus danmakus = new Danmakus();
        if (danmakuListData == null || danmakuListData.length() == 0) {
            return danmakus;
        }
        danmakus = _parse(danmakuListData, danmakus);
        return danmakus;
    }

    private Danmakus _parse(JSONArray jsonArray, Danmakus danmakus) {
        if (danmakus == null) {
            danmakus = new Danmakus();
        }
        if (jsonArray == null || jsonArray.length() == 0) {
            return danmakus;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONArray values = jsonArray.getJSONArray(i);
                if (values.length() > 0) {
                    long time = values.getLong(0) * 1000; // 出现时间
                    int color = ((0x00000000ff000000 | hexStringToColor(values.getString(2))) & 0x00000000ffffffff);// 颜色
                    float textSize = Float.parseFloat(values.getString(3).replace("px", "")); // 字体大小
                    String gravity = values.getString(2);
                    int type = BaseDanmaku.TYPE_SCROLL_RL;
                    if ("center".equals(gravity)) {
                        type = BaseDanmaku.TYPE_FIX_TOP;
                    }
                    BaseDanmaku item = mContext.mDanmakuFactory.createDanmaku(type, mContext);
                    if (item != null) {
                        item.setTime(time);
                        item.textSize = textSize * (mDispDensity - 0.6f);
                        item.textColor = color;
                        item.textShadowColor = color == Color.BLACK ? Color.WHITE : Color.BLACK;
                        item.index = i;
                        item.flags = mContext.mGlobalFlagValues;
                        item.setTimer(mTimer);
                        item.text = values.getString(4);
                        danmakus.addItem(item);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return danmakus;
    }

    public static int hexStringToColor(String hex) {
        // 确保输入字符串是有效的十六进制颜色字符串
        if (hex == null || !hex.startsWith("#") || (hex.length() != 4 && hex.length() != 7)) {
            return 0xfff;
        }

        // 如果是缩写形式（如 #fff），则扩展为 #ffffff
        if (hex.length() == 4) {
            hex = "#" + hex.charAt(1) + hex.charAt(1) +
                    hex.charAt(2) + hex.charAt(2) +
                    hex.charAt(3) + hex.charAt(3);
        }

        // 解析十六进制字符串为整数
        return Integer.parseInt(hex.substring(1), 16);
    }
}