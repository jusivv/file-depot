package org.coodex.fd.security.key;

import org.coodex.util.DigestHelper;
import org.coodex.fd.def.intf.IKeyGenerator;

/**
 * 简易   的密钥生成器
 */
public class SimpleMixKeyGenerator implements IKeyGenerator {
    /**
     * 生成128位密钥
     * @param key   主密钥
     * @param salt  盐
     * @return
     */
    @Override
    public byte[] generate(byte[] key, byte[] salt) {
        byte[] result = new byte[salt.length * 2 + key.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        System.arraycopy(key, 0, result, salt.length, key.length);
        System.arraycopy(salt, 0, result, salt.length + key.length, salt.length);
        return DigestHelper.digestBuff(result, "MD5");
    }

    @Override
    public boolean accept(String tag) {
        return "mixer.simple".equalsIgnoreCase(tag);
    }
}
