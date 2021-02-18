package org.coodex.fd.def.intf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件加密器
 */
public interface IFileCipher extends IProviderSelector {

    /**
     * 获取文件加密输入流
     * @param is    文件输入流
     * @param key   密钥
     * @return      加密输入流
     * @throws IOException
     */
    InputStream getEncryptInputStream(InputStream is, byte[] key) throws IOException;

    /**
     * 获取文件解密输入流
     * @param is    文件输入流
     * @param key   密钥
     * @return      解密输入流
     * @throws IOException
     */
    InputStream getDecryptInputStream(InputStream is, byte[] key) throws IOException;

    /**
     * 获取文件解密输出流
     * @param os    文件输出流
     * @param key   密钥
     * @return      解密输出流
     * @throws IOException
     */
    OutputStream getDecryptOutputStream(OutputStream os, byte[] key) throws IOException;
}
