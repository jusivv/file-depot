package org.vvodes.fd.security.access;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ClientFeedbackAccessController extends AbstractAccessController {
    private static Logger log = LoggerFactory.getLogger(ClientFeedbackAccessController.class);

    private OkHttpClient client;

    public ClientFeedbackAccessController() {
        this.client = new OkHttpClient();
    }

    protected Response getResponse(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        log.debug("response status code: {}, body: {}", response.code(), response.body().toString());
        return response;
    }

    protected abstract boolean getFeedback(String host, String path, String token, String fileId);

    private String getHost(String clientId) {
        String location = profile.getString("access.controller.feedback.location." + clientId,
                "http://127.0.0.1");
        log.debug("client {} location: {}", clientId, location);
        return location;
    }

    @Override
    public boolean canWrite(String clientId, String token) {
        return getFeedback(getHost(clientId), "/writable", token, null);
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return getFeedback(getHost(clientId), "/readable", token, fileId);
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return getFeedback(getHost(clientId),"/deletable", token, fileId);
    }
}
