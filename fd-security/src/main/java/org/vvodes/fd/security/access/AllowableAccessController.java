package org.vvodes.fd.security.access;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.coodex.util.Common;
import org.coodex.util.DigestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * 无限制访问控制器
 */
public class AllowableAccessController extends AbstractAccessController {
    private static Logger log = LoggerFactory.getLogger(AllowableAccessController.class);

    private GoogleAuthenticator googleAuthenticator;

    public AllowableAccessController() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setKeyRepresentation(KeyRepresentation.BASE64).build();
        googleAuthenticator = new GoogleAuthenticator(config);
    }

    @Override
    public boolean canWrite(String clientId, String token) {
        try {
            int t = Integer.parseInt(token);
            String keyBase64 = profile.getString("access.controller.key." + clientId);
            if (Common.isBlank(keyBase64)) {
                log.warn("access controller key for {} is null", clientId);
                return false;
            }
            return googleAuthenticator.authorize(keyBase64, t);
        } catch (NumberFormatException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        String keyBase64 = profile.getString("access.controller.key." + clientId);
        if (Common.isBlank(keyBase64)) {
            log.warn("access controller key for {} is null", clientId);
            return false;
        }

        try {
            return Common.byte2hex(
                    DigestHelper.hmac(fileId.getBytes(Charset.forName("UTF-8")),
                            Base64.getDecoder().decode(keyBase64))
            ).equalsIgnoreCase(token);
        } catch (GeneralSecurityException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean inScope(String clientId, String fileOwner) {
        return true;
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return canRead(clientId, token, fileId);
    }

    @Override
    public boolean accept(String tag) {
        return "allowable".equalsIgnoreCase(tag);
    }
}
