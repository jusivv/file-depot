package org.vvodes.fd.security.aes;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Properties;

/**
 * AES加密输入流
 */
public class AesEncryptInputStream extends InputStream {
    private static Logger log = LoggerFactory.getLogger(AesEncryptInputStream.class);


    private InputStream origin;
    private CryptoCipher cryptoCipher;
    private ByteBuffer inByteBuffer, outByteBuffer;
    private byte[] buff;
    private boolean bufferReady = false;

    public AesEncryptInputStream(InputStream origin, String transform, SecretKeySpec secretKeySpec,
                                 IvParameterSpec iv, Properties properties) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, ShortBufferException,
            IllegalBlockSizeException {
        this.origin = origin;
        cryptoCipher = Utils.getCipherInstance(transform, properties);
        cryptoCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        inByteBuffer = ByteBuffer.allocateDirect(cryptoCipher.getBlockSize() + 2);
        outByteBuffer = ByteBuffer.allocateDirect(cryptoCipher.getBlockSize() + 2);
        buff = new byte[cryptoCipher.getBlockSize()];
        fillBuffer();
    }

    private void fillBuffer() throws IOException, BadPaddingException, ShortBufferException, IllegalBlockSizeException {
        inByteBuffer.clear();
        outByteBuffer.clear();
        int len = origin.read(buff);
        bufferReady = len >= 0;
        if (bufferReady) {
            inByteBuffer.put(buff, 0, len);
            inByteBuffer.flip();
            cryptoCipher.doFinal(inByteBuffer, outByteBuffer);
            outByteBuffer.flip();
        }
    }

    @Override
    public int read() throws IOException {
        if (outByteBuffer.position() >= outByteBuffer.limit()) {
            try {
                fillBuffer();
            } catch (GeneralSecurityException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return bufferReady ? outByteBuffer.get() & 0xFF : -1;
    }

    @Override
    public void close() throws IOException {
        origin.close();
    }
}
