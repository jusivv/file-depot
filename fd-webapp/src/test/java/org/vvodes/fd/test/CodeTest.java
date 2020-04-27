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
        String fileId = "test$cc5c898bdbf74422855fcedf37d060ed";
//        String fileId = "test$e71086ed64fb463fafaeb0a5a788c789,test$cc5c898bdbf74422855fcedf37d060ed";

//        log.debug("TOTP: {}", getTotp(clientId));
        log.debug("downlaod path: /attachments/download/{};c={};t={}",
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
