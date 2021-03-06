package tool.xfy9326.chat.Tools;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

//AES加密算法

public class AES {
    private static final String CipherMode = "AES";

    private static SecretKeySpec createKey(String password) {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(password);
        while (sb.length() < 32) {
            sb.append("0");
        }
        if (sb.length() > 32) {
            sb.setLength(32);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(data, "AES");
    }

    private static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            System.out.println(key);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String content, String password) {
        if (content != null) {
            byte[] data = null;
            try {
                data = content.getBytes("UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            data = encrypt(data, password);
            return byte2hex(data);
        } else {
            return null;
        }
    }

    private static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String content, String password) {
        if (content != null) {
            byte[] data = null;
            try {
                data = hex2byte(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            data = decrypt(data, password);
            if (data == null)
                return null;
            String result = null;
            try {
                result = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            return null;
        }
    }

    private static String byte2hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        String tmp;
        for (byte aB : b) {
            tmp = (Integer.toHexString(aB & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }
}
