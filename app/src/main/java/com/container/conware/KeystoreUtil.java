package com.container.conware;

import android.os.Build;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.crypto.SecretKeyFactory;

public class KeystoreUtil {

    /***
     * Returns all key aliases present in the keystore.
     * @return
     */
    public ArrayList<String> getAllKeyAliases(){
        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);
            Enumeration<String> allKeys = keystore.aliases();
            ArrayList<String> keys = new ArrayList<>();
            for(String k: Collections.list(allKeys)){
                keys.add(k);
            }
            return keys;
        }catch(KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d(Constants.TAG, "Exception occurred");
            return null;
        }
    }

    public String getKeyInfo(String alias){
        StringBuilder keyInfoBuilder = new StringBuilder();
        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);
            Key key = keystore.getKey(alias, null);

            keyInfoBuilder.append("Alias:"+alias);
            keyInfoBuilder.append("\n");
            keyInfoBuilder.append("Algorithm:"+key.getAlgorithm());
            keyInfoBuilder.append("\n");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyInfo keyInfo = null;

                if (isSymmetric(key)) {
                    KeyStore.SecretKeyEntry keyEntry = (KeyStore.SecretKeyEntry) keystore.getEntry(alias, null);
                    SecretKeyFactory factory = SecretKeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore");

                    try {
                        keyInfo = (KeyInfo) factory.getKeySpec(keyEntry.getSecretKey(), KeyInfo.class);
                    } catch (InvalidKeySpecException e) {
                        // Not an Android KeyStore key.
                    }
                } else {
                    KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(alias, null);
                    KeyFactory factory = KeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore");

                    try {
                        keyInfo = factory.getKeySpec(keyEntry.getPrivateKey(), KeyInfo.class);

                    } catch (InvalidKeySpecException e) {
                        // Not an Android KeyStore key.
                    }
                }


                keyInfoBuilder.append("KeySize:" + keyInfo.getKeySize());
                keyInfoBuilder.append("\n");

                String[] blockModes = keyInfo.getBlockModes();
                String[] encryptionPaddings = keyInfo.getEncryptionPaddings();
                String[] signaturePaddings = keyInfo.getSignaturePaddings();
                String[] digests = keyInfo.getDigests();

                if ((blockModes.length != 0)) {
                    keyInfoBuilder.append("BlockModes:" + Arrays.toString(blockModes));
                    keyInfoBuilder.append("\n");
                }
                if (encryptionPaddings.length != 0) {
                    keyInfoBuilder.append("EncryptionPaddings:" + Arrays.toString(encryptionPaddings));
                    keyInfoBuilder.append("\n");
                }
                if (signaturePaddings.length != 0) {
                    keyInfoBuilder.append("SignaturePaddings:" + Arrays.toString(signaturePaddings));
                    keyInfoBuilder.append("\n");
                }
                if (digests.length != 0) {
                    keyInfoBuilder.append("Digests:" + Arrays.toString(digests));
                    keyInfoBuilder.append("\n");
                }

                keyInfoBuilder.append("KeySize:" + getPurpose(keyInfo.getPurposes()));
                keyInfoBuilder.append("\n");
                keyInfoBuilder.append("KeyOrigin:" + getOrigin(keyInfo.getOrigin()));
                keyInfoBuilder.append("\n");
                keyInfoBuilder.append("IsAuthenticationRequired:" + keyInfo.isUserAuthenticationRequired());
                keyInfoBuilder.append("\n");
                keyInfoBuilder.append("UserAuthenticationValidityDurationSeconds:" + keyInfo.getUserAuthenticationValidityDurationSeconds());
                keyInfoBuilder.append("\n");
                keyInfoBuilder.append("IsInsideSecureHardware:" + keyInfo.isInsideSecureHardware());
                keyInfoBuilder.append("\n");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    keyInfoBuilder.append("isInvalidatedByBiometricEnrollment:" + keyInfo.isInvalidatedByBiometricEnrollment());
                    keyInfoBuilder.append("\n");
                    keyInfoBuilder.append("isUserAuthenticationValidWhileOnBody:" + keyInfo.isUserAuthenticationValidWhileOnBody());
                    keyInfoBuilder.append("\n");
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    keyInfoBuilder.append("isTrustedUserPresenceRequired:" + keyInfo.isTrustedUserPresenceRequired());
                    keyInfoBuilder.append("\n");
                    keyInfoBuilder.append("isUserConfirmationRequired:" + keyInfo.isUserConfirmationRequired());
                    keyInfoBuilder.append("\n");
                }
                keyInfoBuilder.append("isUserAuthenticationRequirementEnforcedBySecureHardware:" + keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware());
                keyInfoBuilder.append("\n");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return keyInfoBuilder.toString();
    }
    private boolean isSymmetric(Key key){

        switch(key.getAlgorithm()){
            case "AES":
            case "HMAC":
            case "HmacSHA256":
            case "3DES":
                return true;
            default:
                return false;
        }

    }

    private String getOrigin(int origin){
        switch(origin){
            case KeyProperties.ORIGIN_IMPORTED:
                return "Imported";
            case KeyProperties.ORIGIN_GENERATED:
                return "Generated";
            case KeyProperties.ORIGIN_SECURELY_IMPORTED:
                return "SecurelyImported";
            default:
                return "Unknown Origin";
        }
    }

    private String getPurpose(int purpose){
        StringBuilder keyPurposeBuilder = new StringBuilder();

        if((purpose & KeyProperties.PURPOSE_DECRYPT) != 0)
            keyPurposeBuilder.append(":Decrypt");
        if((purpose & KeyProperties.PURPOSE_ENCRYPT) != 0)
            keyPurposeBuilder.append(":Encrypt");
        if((purpose & KeyProperties.PURPOSE_SIGN) != 0)
            keyPurposeBuilder.append(":Sign");
        if((purpose & KeyProperties.PURPOSE_VERIFY) != 0)
            keyPurposeBuilder.append(":Verify");
        if((purpose & KeyProperties.PURPOSE_WRAP_KEY) != 0)
            keyPurposeBuilder.append(":Wrap Key");

        return keyPurposeBuilder.toString();
    }
}
