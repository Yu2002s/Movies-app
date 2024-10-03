package com.dongyu.movies.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    @Nullable
    public static String encrypt(String mode, String key, String content) {
        try {
            // 将Base64编码的字符串转换成字节数组
            byte[] cipherText;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cipherText = Base64.getDecoder().decode(content);
            } else {
                cipherText = android.util.Base64.decode(content, android.util.Base64.DEFAULT);
            }
            // 创建一个密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
            // 创建一个Cipher实例，指定解密模式为AES/ECB/PKCS5Padding（PKCS5Padding兼容PKCS7Padding）
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(mode);
            // 初始化Cipher实例进行解密
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            // 执行解密
            byte[] decryptedBytes = cipher.doFinal(cipherText);
            return new String(decryptedBytes);
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
            Log.e("jdy", e.toString());
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
            Log.e("jdy", e.toString());
            return null;
        }
    }

}
