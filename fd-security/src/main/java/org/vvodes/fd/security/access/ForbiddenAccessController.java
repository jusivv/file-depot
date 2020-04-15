package org.vvodes.fd.security.access;

import org.vvodes.fd.def.intf.IAccessController;

/**
 * 禁止访问控制器
 */
public class ForbiddenAccessController implements IAccessController {
    @Override
    public boolean canWrite(String clientId, String token) {
        return false;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean inScope(String clientId, String fileOwner) {
        return false;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean accept(String tag) {
        return "forbidden".equalsIgnoreCase(tag);
    }
}
