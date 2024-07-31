package com.dongyu.movies.utils;

import android.content.Context;

import com.dongyu.movies.MoviesApplication;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 应用自身检查器
 */
public class Checker {

  /**
   * App MD5签名
   */
  private static final String APP_SIGN = "45af602790ec8031540c99bde7168827";

  public static void verifySignature() {
    verifySignature(MoviesApplication.context);
  }

  public static void verifySignature(Context context) {

    // 验证App签名是否被修改
    if (!APP_SIGN.equals(getAppSignatureMD5(context))) {
      try {
        Class<?> aClass = Class.forName("java.lang.System");
        Method method = aClass.getDeclaredMethod("exit", int.class);
        method.invoke(null, 0);
      } catch (Exception ignored) {
      }
    }
  }

  public static String getAppSignatureMD5() {
    return getAppSignatureMD5(MoviesApplication.context);
  }

  /**
   * 获取App的md5签名
   *
   * @return md5值
   */
  @SuppressWarnings("all")
  public static String getAppSignatureMD5(Context context) {
    try {
      // Context context = MoviesApplication.context;
      Class<?> contextClass = context.getClass();
      Method getPackageManager = contextClass.getMethod("getPackageManager");
      Object pm = getPackageManager.invoke(context);
      Class<?> packgeManagerClass = Class.forName("android.content.pm.PackageManager");
      Method getPackageInfo = packgeManagerClass.getMethod("getPackageInfo", String.class, int.class);
      Object packageInfo = getPackageInfo.invoke(pm, "com.dongyu.movies", 0x00000040);
      Class<?> packageInfoClass = Class.forName("android.content.pm.PackageInfo");
      Field signatures = packageInfoClass.getField("signatures");
      Object signaturesArr = signatures.get(packageInfo);
      assert signaturesArr != null;
      Object signature = Array.get(signaturesArr, 0);
      Class<?> messageDigestClass = Class.forName("java.security.MessageDigest");
      Method getInstance = messageDigestClass.getMethod("getInstance", String.class);
      Object messageDigest = getInstance.invoke(null, "MD5");
      Class<?> signatureClass = Class.forName("android.content.pm.Signature");
      Method toByteArray = signatureClass.getDeclaredMethod("toByteArray");
      Object bytes = toByteArray.invoke(signature);
      Method update = messageDigestClass.getMethod("update", byte[].class);
      update.invoke(messageDigest, bytes);
      Method digest = messageDigestClass.getMethod("digest");
      byte[] digests = (byte[]) digest.invoke(messageDigest);
      assert digests != null;
      return bytesToHex(digests);
    } catch (Exception ignored) {
    }
    return null;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

}
