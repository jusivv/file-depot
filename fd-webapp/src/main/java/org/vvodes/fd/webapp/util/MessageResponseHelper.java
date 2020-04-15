package org.vvodes.fd.webapp.util;

import org.vvodes.fd.webapp.pojo.ResponseMessage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

public class MessageResponseHelper {
    public static void resume(int statusCode, String code, String message, AsyncResponse asyncResponse) {
        asyncResponse.resume(
                Response.status(statusCode).header("Content-Type", "application/json").entity(
                        new ResponseMessage(code, message)).build());
    }

    public static void resume(int statusCode, String message, AsyncResponse asyncResponse) {
        asyncResponse.resume(
                Response.status(statusCode).header("Content-Type", "application/json").entity(
                        new ResponseMessage(String.valueOf(statusCode), message)).build());
    }
}
