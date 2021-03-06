package org.coodex.fd.security.access;

import org.coodex.util.Profile;
import org.coodex.fd.def.intf.IAccessController;

public abstract class AbstractAccessController implements IAccessController {
    protected static Profile profile = Profile.get("config.properties");

    @Override
    public boolean inScope(String clientId, String fileOwner) {
        if (clientId.equals(fileOwner)) {
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
}
