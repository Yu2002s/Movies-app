package com.dongyu.movies.utils;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Nullable
    public static String encrypt(String mode, String key, String content) {
        try {
            // 创建一个密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(key), "AES");
            // 创建一个Cipher实例，指定解密模式为AES/ECB/PKCS5Padding（PKCS5Padding兼容PKCS7Padding）
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(mode);
            // 初始化Cipher实例进行解密
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            // 执行解密
            byte[] decryptedBytes = cipher.doFinal(content.getBytes());
            return Base64.encodeToString(decryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String encrypt(String mode, String key, String iv, String content) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        try {
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String mode, String key, String iv, String content) {
        try {
            byte[] bytes = android.util.Base64.decode(content, android.util.Base64.DEFAULT);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));//强烈注意：CBC必须要

            Cipher cipher = Cipher.getInstance(mode);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);			// 初始化
            byte[] result = cipher.doFinal(bytes);
            return new String(result);
        } catch (Exception e) {
            return null;
        }
    }

}
