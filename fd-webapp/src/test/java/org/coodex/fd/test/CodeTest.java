package org.coodex.fd.test;

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
        // config
        String host = "http://localhost:8090";
        String clientId = "test";
//        String host = "https://filedepot.etcmall.cn";

        // upload
        uploadTest(host, clientId, true);
//        log.debug("file upload path: {}/attachments/upload/byform/{}/{}/{}", clientId, getTotp(clientId), 1);
//        log.debug("file upload path: {}/attachments/upload/byform/{}/{}/{}", clientId, getTotp(clientId), 1);
//        log.debug("TOTP: {}", getTotp(clientId));

        // download
//        downloadTest(host, clientId, "test$295188c117fc44d380789090a7c0b7b4");
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

    private static void uploadTest(String host, String clientId, boolean encrypt) {
        int uploadToken = getTotp(clientId);
        int enc = encrypt ? 1 : 0;
        log.info("upload token: {}", uploadToken);
        log.info("\r\n# file depot test - upload\r\n## by form\r\n" +
                "### uri\r\n```\r\n{}/attachments/upload/byform/{}/{}/{}\r\n```\r\n\r\n" +
                "### curl\r\n```\r\ncurl -v -X POST -F \"file=@{}\" {}/attachments/upload/byform/{}/{}/{}\r\n```\r\n\r\n" +
                "## by base64\r\n### uri\r\n```\r\n{}/attachments/upload/bybase64\r\n```\r\n\r\n### json\r\n```JSON\r\n" +
                "{\r\n\"clientId\": \"{}\",\r\n\"token\": \"{}\",\r\n\"fileName\": \"{}\"," +
                "\r\n\"contentType\": \"{}\",\r\n\"base64File\": \"{}\",\r\n\"encrypt\": {}\r\n}\r\n```\r\n\r\n" +
                "### curl\r\n```\r\ncurl -v -H \"Content-Type: application/json\" -X POST " +
                "-d \"{clientId:'{}',token:'{}',fileName:'{}',contentType:'{}',base64File:'{}',encrypt:{}}\" " +
                "{}/attachments/upload/bybase64\r\n```\r\n\r\n", host, clientId, uploadToken, enc, "<local file path>",
                host, clientId, uploadToken, enc, host, clientId, uploadToken, "<filename>", "<file content type>",
                "<base64>", encrypt ? "true" : "false", clientId, uploadToken, "<filename>", "<file content type>",
                "<base64>", encrypt ? "true" : "false", host);
    }

    private static void downloadTest(String host, String clientId, String fileId) throws NoSuchAlgorithmException, InvalidKeyException {
        int downloadToken = getDownloadTotp(clientId, fileId);
        log.info("\r\n# file depot test - download\r\n## stream\r\n" +
                "### uri\r\n```\r\n{}/attachments/download/{};c={};t={}\r\n```\r\n\r\n" +
                "### curl\r\n```\r\ncurl -v -X GET -o \"{}\" {}/attachments/download/{}\\;c={}\\;t={}\r\n```\r\n\r\n" +
                "## base64\r\n### uri\r\n```\r\n{}/attachments/base64/{};c={};t={}\r\n```\r\n\r\n" +
                "### curl\r\n```\r\ncurl -v -X GET {}/attachments/base64/{}\\;c={}\\;t={}\r\n```\r\n\r\n", host, fileId,
                clientId, downloadToken, "<local file path>", host, fileId.replaceAll("\\$", "\\\\\\$"),
                clientId, downloadToken, host, fileId, clientId, downloadToken, host,
                fileId.replaceAll("\\$", "\\\\\\$"), clientId, downloadToken);
    }
}
