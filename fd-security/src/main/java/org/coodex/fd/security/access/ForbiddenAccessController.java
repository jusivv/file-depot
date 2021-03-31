package org.coodex.fd.security.access;

import org.coodex.fd.def.intf.IAccessController;

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

    @Override
    public boolean notify(String clientId, String token, String fileId) {
        return true;
    }
}
