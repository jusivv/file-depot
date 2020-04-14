package org.vvodes.fd.security;

import org.vvodes.fd.def.intf.IAccessController;

/**
 * 无限制访问控制器
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
    public boolean allowRead(String clientId, String fileOwner) {
        return true;
    }

    @Override
    public boolean accept(String tag) {
        return tag.equalsIgnoreCase("allowable");
    }
}
