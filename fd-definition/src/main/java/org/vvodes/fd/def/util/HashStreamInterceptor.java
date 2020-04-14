package org.vvodes.fd.def.util;

import org.vvodes.fd.def.intf.IStreamInterceptor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashStreamInterceptor implements IStreamInterceptor {

    private MessageDigest digest;
    private boolean finished;
    private String hashValue;

    public HashStreamInterceptor(String algorithm) throws NoSuchAlgorithmException {
        this.digest = MessageDigest.getInstance(algorithm);
        this.finished = false;
    }

    public int load(int originData, int processData) {
        if (digest != null) {
            digest.update((byte) originData);
        }
        return processData;
    }

    public void finish() {
        if (!finished) {
            finished = true;
            hashValue = bytesToHex(digest.digest());
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public String getHashValue() {
        return hashValue;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
}
