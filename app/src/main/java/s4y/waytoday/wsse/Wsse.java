package s4y.waytoday.wsse;

import android.util.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Date;

import s4y.waytoday.errors.ErrorsObservable;

public class Wsse {
    static private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static private String base64(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    static private byte[] sha1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(text.getBytes(UTF8_CHARSET));
            return md.digest();
        } catch (Exception e) {
            ErrorsObservable.notify(e, true);
            return null;
        }
    }

    private static byte[] digest(String password, String nonce, String created) {
        String text = nonce + created + password;
        return sha1(text);
    }

    public static String getToken() {
        String nonce = String.valueOf(Math.random());
        String created = new Date().toString();
        return "Username=\"s4y.waytoday\"," +
                "PasswordDigest=\"" + base64(digest(Secret.get(), nonce, created)) + "\"," +
                "Nonce=\"" + base64(nonce.getBytes()) + "\"," +
                "Created=\"" + created + "\"";
    }
}
