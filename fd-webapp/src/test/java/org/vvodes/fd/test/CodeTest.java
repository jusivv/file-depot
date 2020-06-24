package org.vvodes.fd.test;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.coodex.util.DigestHelper;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CodeTest {
    private static Logger log = LoggerFactory.getLogger(CodeTest.class);

    private static Profile profile = Profile.get("config.properties");

    public static void main(String[] args) throws Exception {
        String clientId = "test";
        String fileId = "test$5cbc9a3ec3e14df9adda3a24b80f0f75";
//        String fileId = "test$870a83506f9b4a2da4ec610939d743b2,test$d02f7374a553462a87db568fdd9a5eeb";

        log.debug("TOTP: {}", getTotp(clientId));
        String host = "http://127.0.0.1:8090";
//        String host = "https://filedepot.etcmall.cn";
        log.debug("downlaod path: {}/attachments/download/{};c={};t={}", host,
                fileId, clientId, getDownloadTotp(clientId, fileId));
    }

    private static int getTotp(String clientId) {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setKeyRepresentation(KeyRepresentation.BASE64).setCodeDigits(6).build();
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(config);
        return googleAuthenticator.getTotpPassword(profile.getString("access.controller.key." + clientId));
    }

    private static int getDownloadTotp(String clientId, String fileId) throws InvalidKeyException,
            NoSuchAlgorithmException {
        String secretBase64 = Base64.getEncoder().encodeToString(
                DigestHelper.hmac(fileId.getBytes(Charset.forName("UTF-8")),
                        Base64.getDecoder().decode(profile.getString("access.controller.key." + clientId))));
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setKeyRepresentation(KeyRepresentation.BASE64).setCodeDigits(6).build();
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(config);
        return googleAuthenticator.getTotpPassword(secretBase64);
    }
}
