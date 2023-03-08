package com.HKJC.Hash;

import java.math.BigInteger;
import java.security.MessageDigest;

public class HashStr {
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static int hash(String str) throws Exception {
        BigInteger nr_bins = new BigInteger("999999");
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(str.getBytes("UTF-8"));
        String hexdigest = toHexString(digest);
        BigInteger intDigest = new BigInteger(hexdigest.trim(), 16);
        BigInteger hash = intDigest.mod(nr_bins);
        return hash.intValue() + 1;
    }

    public static int parse_sparse_field(String field_str) {
        try {
            return Integer.parseInt(field_str);
        } catch (NumberFormatException e) {
            try {
                return hash(field_str);
            } catch (Exception e1) {
                throw e;
            }
        } catch (Exception e) {
            return 0;
        }
    }

}
