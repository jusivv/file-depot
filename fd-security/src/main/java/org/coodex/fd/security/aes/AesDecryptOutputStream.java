package org.coodex.fd.security.aes;

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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Properties;

public class AesDecryptOutputStream extends OutputStream {
    private static Logger log = LoggerFactory.getLogger(AesDecryptOutputStream.class);

    private OutputStream origin;
    private CryptoCipher cryptoCipher;
    private ByteBuffer inByteBuffer, outByteBuffer;
    private byte[] buff;

    public AesDecryptOutputStream(OutputStream origin, String transform, SecretKeySpec secretKeySpec,
                                  IvParameterSpec iv, Properties properties) throws IOException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        this.origin = origin;
        cryptoCipher = Utils.getCipherInstance(transform, properties);
        cryptoCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        inByteBuffer = ByteBuffer.allocateDirect(cryptoCipher.getBlockSize());
        outByteBuffer = ByteBuffer.allocateDirect(cryptoCipher.getBlockSize());
        buff = new byte[cryptoCipher.getBlockSize()];
    }

    private void writeToOrigin() throws BadPaddingException, ShortBufferException, IllegalBlockSizeException,
            IOException {
        inByteBuffer.flip();
        outByteBuffer.clear();
        cryptoCipher.doFinal(inByteBuffer, outByteBuffer);
        outByteBuffer.flip();
        while (outByteBuffer.position() < outByteBuffer.limit()) {
            origin.write(outByteBuffer.get());
        }
        inByteBuffer.clear();
    }

    @Override
    public void write(int b) throws IOException {
        inByteBuffer.put((byte) b);
        if (inByteBuffer.position() == inByteBuffer.limit()) {
            try {
                writeToOrigin();
            } catch (GeneralSecurityException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        origin.flush();
    }

    @Override
    public void close() throws IOException {
        if (inByteBuffer.position() > 0) {
            try {
                writeToOrigin();
            } catch (GeneralSecurityException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        }
        origin.close();
    }
}
