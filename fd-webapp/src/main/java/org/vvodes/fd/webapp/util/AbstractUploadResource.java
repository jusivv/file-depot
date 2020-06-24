package org.vvodes.fd.webapp.util;

import org.vvodes.fd.def.intf.IAccessController;
import org.vvodes.fd.def.intf.IFileCipher;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractUploadResource {
    /**
     * 判断是否有写权限
     * @param clientId  终端ID
     * @param token     令牌
     * @return          是否允许写
     */
    protected boolean canWrite(String clientId, String token) {
        IAccessController accessControler = ComponentBuiler.getAccessController(clientId);
        return accessControler.canWrite(clientId, token);
    }

    /**
     * 获取加密输入流
     * @param is            源输入流
     * @param cipherModel   加密模式
     * @param salt          盐
     * @return              加密输入流
     * @throws IOException
     */
    protected InputStream getEncryptStream(InputStream is, String cipherModel, String salt)
            throws IOException {
        // key
        byte[] key = ComponentBuiler.getKey(cipherModel, salt);
        // cipher
        IFileCipher fileCipher = ComponentBuiler.getFileCipher(cipherModel);
        return fileCipher.getEncryptInputStream(is, key);
    }
}
