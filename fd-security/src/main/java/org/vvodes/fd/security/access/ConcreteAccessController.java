package org.vvodes.fd.security.access;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 适配Concrete的访问控制器
 */
public class ConcreteAccessController extends AbstractAccessController {
    private static Logger log = LoggerFactory.getLogger(ConcreteAccessController.class);

    private OkHttpClient client;

    public ConcreteAccessController() {
        client = new OkHttpClient();
    }

    private String getUrl(String path, String clientId) {
        String location = profile.getString("access.controller.concrete.location." + clientId,
                "http://127.0.0.1");
        log.debug("client {} location: {}", clientId, location);
        StringBuilder sb = new StringBuilder(location);
        sb.append(location.endsWith("/") ? "Client" : "/Client").append(path);
        return sb.toString();
    }

    private boolean getResult(String path, String clientId) {
        HttpUrl httpUrl = HttpUrl.parse(getUrl(path, clientId));
        Request request = new Request.Builder().url(httpUrl).get().build();
        try {
            Response response = client.newCall(request).execute();
            return response.code() == 200 && response.body().string().equalsIgnoreCase("true");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean canWrite(String clientId, String token) {
        return getResult("/writable/" + token, clientId);
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return getResult("/readable/" + token + "/" + fileId, clientId);
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return getResult("/deletable/" + token + "/" + fileId, clientId);
    }

    @Override
    public boolean accept(String tag) {
        return "concrete".equalsIgnoreCase(tag);
    }
}
