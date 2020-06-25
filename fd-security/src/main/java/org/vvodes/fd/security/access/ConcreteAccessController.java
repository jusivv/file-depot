package org.vvodes.fd.security.access;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 适配Concrete的访问控制器
 */
public class ConcreteAccessController extends ClientFeedbackAccessController {
    private static Logger log = LoggerFactory.getLogger(ConcreteAccessController.class);

    @Override
    protected Request buildRequest(String url, String token, String fileId) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("/").append(token);
        if (fileId != null) {
            sb.append("/").append(fileId);
        }
        HttpUrl httpUrl = HttpUrl.parse(sb.toString());
        return new Request.Builder().url(httpUrl).get().build();
    }

    @Override
    protected boolean parseResponse(Response response) {
        try {
            return response.code() == 200 && response.body().string().equalsIgnoreCase("true");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean accept(String tag) {
        return "concrete".equalsIgnoreCase(tag);
    }
}
