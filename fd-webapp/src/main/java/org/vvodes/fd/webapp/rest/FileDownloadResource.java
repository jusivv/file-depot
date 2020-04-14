package org.vvodes.fd.webapp.rest;

import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vvodes.fd.def.intf.IAccessController;
import org.vvodes.fd.def.intf.IFileCipher;
import org.vvodes.fd.def.intf.IFileRepository;
import org.vvodes.fd.def.pojo.CommonFileInfo;
import org.vvodes.fd.def.pojo.StoreFileInfo;
import org.vvodes.fd.webapp.ComponentBuiler;
import org.vvodes.fd.webapp.pojo.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Path("attachments/download")
public class FileDownloadResource {
    private static Logger log = LoggerFactory.getLogger(FileDownloadResource.class);
    private static Profile profile = Profile.get("config.properties");

    private IFileRepository fileRepository;

    @Autowired
    public FileDownloadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
    }

    private OutputStream getDecryptStream(OutputStream os, String cipherModel, String salt)
            throws IOException {
        // key
        byte[] key = ComponentBuiler.getKey(cipherModel, salt);
        // cipher
        IFileCipher fileCipher = ComponentBuiler.getFileCipher(cipherModel);
        return fileCipher.getDecryptOutputStream(os, key);
    }

    private void forbidden(AsyncResponse asyncResponse) {
        asyncResponse.resume(
                Response.status(403).entity(
                        new ErrorResponse("403", "Access Forbidden.")).build());
    }

    private String getContentDispType(CommonFileInfo fileInfo) {
        String contentType = fileInfo.getContentType();
        return contentType.startsWith("text") || contentType.startsWith("image") ? "inline"
                : "attachment";
    }

    @GET
    @Path("/{fileId}")
    public void download(
            @Suspended final AsyncResponse asyncResponse,
            @MatrixParam("c") final String clientId,
            @MatrixParam("t") final String token,
            @PathParam("fileId") final String fileId) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IAccessController accessController = ComponentBuiler.getAccessController(clientId);
                    if (accessController.canRead(clientId, token, fileId)) {
                        final StoreFileInfo fileInfo = fileRepository.getFileInfo(fileId);
                        if (accessController.allowRead(clientId, fileInfo.getOwner())) {
                            Response.ResponseBuilder builder = Response.ok()
                                    .header("Content-Type", fileInfo.getContentType());
                            try {
                                builder.header("Content-Disposition",
                                        getContentDispType(fileInfo) + ";filename\"" +
                                                URLEncoder.encode(fileInfo.getOriginName(), "UTF-8") +
                                                "\"");
                            } catch (UnsupportedEncodingException e) {
                                log.error(e.getLocalizedMessage(), e);
                                throw new RuntimeException(e);
                            }
                            StreamingOutput output = new StreamingOutput() {
                                @Override
                                public void write(OutputStream output) throws IOException, WebApplicationException {
                                    OutputStream os = output;
                                    if (!Common.isBlank(fileInfo.getCipherModel())) {
                                        // key
                                        byte[] key = ComponentBuiler.getKey(fileInfo.getCipherModel(), fileInfo.getSalt());
                                        // cipher
                                        IFileCipher fileCipher = ComponentBuiler.getFileCipher(fileInfo.getCipherModel());
                                        os = fileCipher.getDecryptOutputStream(output, key);
                                    }
                                    try {
                                        fileRepository.fetch(os, fileInfo.getFileId());
                                    } finally {
                                        os.close();
                                    }
                                }
                            };
                            asyncResponse.resume(builder.entity(output).build());
                        } else {
                            forbidden(asyncResponse);
                        }
                    } else {
                        forbidden(asyncResponse);
                    }
                } catch (Throwable t) {
                    log.error(t.getLocalizedMessage(), t);
                    asyncResponse.resume(Response.status(500).entity(t.getLocalizedMessage()).build());
                }
            }
        });
        t.setPriority(5);
        t.start();
    }
}
