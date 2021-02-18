package org.coodex.fd.security.aes;

import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.coodex.fd.def.intf.IFileCipher;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * AES文件加密器
 */
public class AesV1FileCipher implements IFileCipher {
    private static Logger log = LoggerFactory.getLogger(AesV1FileCipher.class);

    private static final String TRANSFORM = "AES/CFB/NoPadding";

    private static final IvParameterSpec IV = new IvParameterSpec("0123456789ABCDEF".getBytes(Charset.forName("UTF-8")));

    private Properties properties = new Properties();

    public AesV1FileCipher() {
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY,
                CryptoCipherFactory.CipherProvider.JCE.getClassName());
    }

    @Override
    public InputStream getEncryptInputStream(InputStream is, byte[] key) throws IOException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        try {
            return new AesEncryptInputStream(is, TRANSFORM, secretKeySpec, IV, properties);
        } catch (GeneralSecurityException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getDecryptInputStream(InputStream is, byte[] key) throws IOException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return new CryptoInputStream(TRANSFORM, properties, is, secretKeySpec, IV);
    }

    @Override
    public OutputStream getDecryptOutputStream(OutputStream os, byte[] key) throws IOException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        try {
            return new AesDecryptOutputStream(os, TRANSFORM, secretKeySpec, IV, properties);
        } catch (GeneralSecurityException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean accept(String tag) {
        return "aes.v1".equalsIgnoreCase(tag);
    }
}
