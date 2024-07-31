package com.dongyu.movies.utils;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryption {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
  private static final int KEY_SIZE = 256; // AES key size in bits
  private static final int ITERATIONS = 65536; // Number of iterations for PBKDF2
  private static final String SALT = "_o?~WG*l!{xaqd5Imbv^"; // A unique salt value for PBKDF2

  private static final String IV = "!W83P^$YFFk1&w1S";

  // 23*hHVh5Ec0o4M4!9i9AEx@68&h9J$MW
  // 此处生成的为假密码
  private static final String PASSWORD = EncryptUtils.getInstance().decode("xLW1rrgeroh1iOnaxKr1LM4LeTzO1pXkoHmqRRW3p5ULSwS5dbw8UuhITd3Dov44");

  public static String encrypt(String plaintext) {
    return encrypt(PASSWORD, plaintext);
  }

  public static String decrypt(String plaintext) {
    return decrypt(PASSWORD, plaintext);
  }

  public static String encrypt(String password, String plaintext) {
    try {
      // Derive a key from the password
      SecretKey secretKey = deriveKey(password);

      // Initialize cipher for encryption
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes()));

      // Encrypt the plaintext
      byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      // Return the Base64 encoded encrypted bytes
      return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    } catch (Exception ignored) {}
    return null;
  }

  public static String decrypt(String password, String encryptedText) {
    try {
      // Derive a key from the password
      SecretKey secretKey = deriveKey(password);

      // Decode the encrypted text
      byte[] encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);

      // Initialize cipher for decryption
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes()));

      // Decrypt the encrypted bytes
      byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

      // Return the decrypted plaintext
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception ignored) {}
    return null;
  }

  private static SecretKey deriveKey(String password) throws Exception {
    // Create a PBEKeySpec with the password, salt, iteration count, and key length
    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(StandardCharsets.UTF_8), ITERATIONS, KEY_SIZE);

    // Create a SecretKeyFactory and generate a SecretKey
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    SecretKey secretKey = factory.generateSecret(keySpec);

    // Convert the SecretKey to a SecretKeySpec for AES
    return new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
  }

}