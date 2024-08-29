# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-keepclasseswithmembers class com.youku.multiscreen.MultiScreen{*;}
#-keepclasseswithmembers class com.yunos.tvhelper.support.biz.ut.Ut.** {*;}
#-keepclasseswithmembers class com.youku.multiscreen.Client{*;}
#-keepclasseswithmembers class com.youku.multiscreen.callback.** {*;}
#-keepclasseswithmembers class com.yunos.** {*;}
#-keepclasseswithmembers class com.tmalltv.** {*;}
#-keep class * extends com.yunos.lego.LegoBundle
#-keep class * extends com.yunos.tvhelper.youku.dlna.biz.cb.DlnaCb
#-keep class * extends com.yunos.tvhelper.youku.remotechannel.api.RchannelPublic$IRchannel
#
#-keepclasseswithmembers class com.ta.utdid2.** { *;}
#-keepclasseswithmembers class com.ta.audid.** {*;}
#-keepclasseswithmembers class com.ut.device.** { *;}
#-keepclasseswithmembers class com.ut.mini.** {*;}
#-keep public class com.alibaba.mtl.log.model.LogField {public *;}
#-keepclasseswithmembers public class com.alibaba.analytics.core.model.LogField{*;}
#-keepclasseswithmembers class com.ut.mini.exposure.TrackerFrameLayout$ExposureEntity{*;}
#
#-keep class com.alibaba.fastjson.** {*;}
#-keep class com.aliyun.player.aliplayerscreenprojection.** {*;}