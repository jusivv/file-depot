package org.vvodes.fd.security;

/**
 * 只读访问控制器
 */
public class ReadOnlyAccessController extends AbstractAccessController {
    @Override
    public boolean canWrite(String clientId, String token) {
        return false;
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return true;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return false;
    }

    @Override
    public boolean accept(String tag) {
        return "readOnly".equalsIgnoreCase(tag);
    }
}
