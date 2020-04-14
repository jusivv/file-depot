package org.vvodes.fd.def.intf;

/**
 * 访问控制器
 */
public interface IAccessController extends IProviderSelector {
    /**
     * 是否可写
     * @param clientId  终端ID
     * @param token     令牌
     * @return          是否可写
     */
    boolean canWrite(String clientId, String token);

    /**
     * 是否可读
     * @param clientId  终端ID
     * @param token     令牌
     * @param fileId    文件ID
     * @return          是否可写
     */
    boolean canRead(String clientId, String token, String fileId);

    /**
     * 是否允许读
     * @param clientId  终端ID
     * @param fileOwner 文件属主
     * @return          是否允许读
     */
    boolean allowRead(String clientId, String fileOwner);
}
