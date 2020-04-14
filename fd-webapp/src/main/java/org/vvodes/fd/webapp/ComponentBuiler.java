package org.vvodes.fd.webapp;

import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.vvodes.fd.def.intf.IAccessController;
import org.vvodes.fd.def.intf.IFileCipher;
import org.vvodes.fd.def.intf.IKeyGenerator;
import org.vvodes.fd.def.util.ServiceHelper;

import java.nio.charset.Charset;
import java.util.Base64;

public class ComponentBuiler {
    private static Profile profile = Profile.get("config.properties");

    public static IAccessController getAccessController(String clientId) {
        IAccessController accessControler = ServiceHelper.getProvider(
                profile.getString("access.controller." + clientId, "forbidden"),
                IAccessController.class);
        if (accessControler == null) {
            throw new RuntimeException("access controler for [" + clientId + "] is null");
        }
        return accessControler;
    }

    public static IFileCipher getFileCipher(String cipherModel) {
        IFileCipher fileCipher = ServiceHelper.getProvider(cipherModel, IFileCipher.class);
        if (fileCipher == null) {
            throw new RuntimeException("fail to encrypt file, file cipher is null.");
        }
        return fileCipher;
    }

    public static byte[] getKey(String cipherModel, String salt) {
        String serverKey = profile.getString("file.cipher." + cipherModel + ".server.key");
        if (Common.isBlank(serverKey)) {
            throw new RuntimeException("fail to encrypt file, empty server key.");
        }
        IKeyGenerator keyGenerator = ServiceHelper.getProvider(
                profile.getString("file.cipher." + cipherModel + ".key.generator",
                        "mixer.simple"), IKeyGenerator.class);
        if (keyGenerator == null) {
            throw new RuntimeException("fail to encrypt file, key generator is null.");
        }
        return keyGenerator.generate(Base64.getDecoder().decode(serverKey),
                salt.getBytes(Charset.forName("UTF-8")));
    }
}
