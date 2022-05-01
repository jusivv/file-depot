package org.coodex.fd.security.access;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonFeedbackAccessController extends ClientFeedbackAccessController {
    private static Logger log = LoggerFactory.getLogger(CommonFeedbackAccessController.class);

    @Override
    protected Request buildRequest(String url, String token, String fileId) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"),
                JSON.toJSONString(fileId != null ? new FileReq(token, fileId) : new TokenReq(token)));
        return new Request.Builder().url(httpUrl).post(requestBody).build();
    }

    @Override
    protected boolean parseResponse(Response response) {
        return super.successStatus(response.code());
    }

    @Override
    public boolean accept(String tag) {
        return "common-feedback".equalsIgnoreCase(tag);
    }

    private class TokenReq {
        private String token;

        public TokenReq(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private class FileReq extends TokenReq {

        private String fileId;

        public FileReq(String token, String fileId) {
            super(token);
            this.fileId = fileId;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }
    }
}
