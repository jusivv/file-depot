package org.vvodes.fd.security;

import org.vvodes.fd.def.intf.IAccessController;

/**
 * 无限制访问控制器 // TODO 加令牌校验
 */
public class AllowableAccessController implements IAccessController {
    @Override
    public boolean canWrite(String clientId, String token) {
        return true;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean inScope(String clientId, String fileOwner) {
        return true;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean accept(String tag) {
        return "allowable".equalsIgnoreCase(tag);
    }
}
