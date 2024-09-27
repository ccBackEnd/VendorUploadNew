package com.application.main.Service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
@Service
public class EncDecService {

	    public static final String ALGORITHM = "AES";
	    public static final int KEY_SIZE = 256;
	   	   

	    // Encrypt a string using AES encryption
	    public static String encrypt(String data, SecretKey secretKey) throws Exception {
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
	        return Base64.getEncoder().encodeToString(encryptedBytes);
	    }

	    // Decrypt a string using AES decryption
	    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);
	        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
	        return new String(decryptedBytes);
	    }

		public EncDecService() {
			super();
		}

	   
	}

