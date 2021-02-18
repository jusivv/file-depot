package org.coodex.fd.def.intf;

/**
 * 分散密钥生成器
 */
public interface IKeyGenerator extends IProviderSelector{
    /**
     * 生成分散密钥
     * @param key   主密钥
     * @param salt  盐
     * @return      分散密钥
     */
    byte[] generate(byte[] key, byte[] salt);
}
