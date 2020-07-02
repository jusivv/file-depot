package org.vvodes.fd.security.access;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcreteAccessControllerV040 extends ConcreteAccessController {
    private static Logger log = LoggerFactory.getLogger(ConcreteAccessControllerV040.class);

    @Override
    protected Request buildRequest(String url, String token, String fileId) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        String content = fileId != null ?
                String.format("{\"attachmentId\":\"%s\", \"token\":\"%s\"}", fileId, token) :
                token;
        return new Request.Builder().url(httpUrl)
                .post(RequestBody.create(MediaType.get("application/json"), content)).build();
    }



    @Override
    public boolean accept(String tag) {
        return "concrete_v0.4.0".equals(tag);
    }
}
