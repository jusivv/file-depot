package org.vvodes.fd.security;

import org.coodex.util.Profile;
import org.vvodes.fd.def.intf.IAccessController;

/**
 * 只读访问控制器
 */
public class ReadOnlyAccessController implements IAccessController {
    private static Profile profile = Profile.get("config.properties");
    @Override
    public boolean canWrite(String clientId, String token) {
        return false;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean allowRead(String clientId, String fileOwner) {
        if (clientId == fileOwner) {
            return true;
        }
        String[] scopes = profile.getStrList("access.scope." + clientId);
        if (scopes != null) {
            for (String owner : scopes) {
                if (owner.equalsIgnoreCase("*") || owner.equalsIgnoreCase(fileOwner)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean accept(String tag) {
        return tag.equalsIgnoreCase("readOnly");
    }
}
