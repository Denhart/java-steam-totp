import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.Mac;
import org.apache.commons.codec.binary.Hex;

/**
 * Created by DenPC on 16-12-2015.
 */
public class SteamTOTP {
    private String sharedSecret;
    private String identitySecret;
    private int timeDiff;

    public SteamTOTP(String sharedSecret, String identitySecret, int timeDiff) {
        this.sharedSecret = sharedSecret;
        this.identitySecret = identitySecret;
        this.timeDiff = timeDiff;
    }

    public String getAuthCode() throws NoSuchAlgorithmException {
        long unixTime = (System.currentTimeMillis() / 1000L) + timeDiff;
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = Base64.decode(sharedSecret, Base64.DEFAULT);
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            int time = (int) (unixTime/30);
            ByteBuffer b = ByteBuffer.allocate(8);
            b.putInt(4, time);
            b.order(ByteOrder.BIG_ENDIAN);
            byte[] result = b.array();
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(result);
            byte start = (byte) (rawHmac[19] & 0x0F);

            byte[] bytes = new byte[4];
            bytes = Arrays.copyOfRange(rawHmac, start, start+4);
            ByteBuffer wrapped = ByteBuffer.wrap(bytes);
            int codeInt = wrapped.getInt();
            int fullcode = (codeInt & 0x7fffffff) & 0x00000000ffffffff;

            char[] STEAMCHARS = new char[] {
                    '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C',
                    'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
                    'R', 'T', 'V', 'W', 'X', 'Y'};
            String chars = "23456789BCDFGHJKMNPQRTVWXY";
            String code = "";
            for(int i = 0; i < 5; i++) {
                String curChar = String.valueOf(chars.charAt(fullcode % chars.length()));
                code = code + curChar;
                fullcode /= chars.length();
            }
            return code;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getConfirmationKey(int unixTime, String tag) {
        // Get an hmac_sha1 key from the raw key bytes
        byte[] keyBytes = Base64.decode(identitySecret, Base64.DEFAULT);
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        int dataLen = 8;

        if(!tag.isEmpty()) {
            if(tag.length() > 32) {
                dataLen += 32;
            } else {
                dataLen += tag.length();
            }
        }

        ByteBuffer b = ByteBuffer.allocate(dataLen);
        b.putInt(4, unixTime);
        b.order(ByteOrder.BIG_ENDIAN);
        byte[] result = b.array();
        b.position(8);
        b.put(tag.getBytes());

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(result);
            String Hmac = Base64.encodeToString(rawHmac,Base64.DEFAULT);
            return Hmac;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
}
