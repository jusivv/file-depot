package org.coodex.fd.webapp.rest;

import org.coodex.fd.webapp.pojo.Base64DownloadResp;
import org.coodex.fd.webapp.util.FileDepotWebException;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.coodex.fd.def.intf.IFileRepository;
import org.coodex.fd.def.pojo.StoreFileInfo;
import org.coodex.fd.webapp.util.AbstractDownloadResource;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;

@Path("attachments/base64")
public class Base64DownloadResource extends AbstractDownloadResource {

    private static Logger log = LoggerFactory.getLogger(Base64DownloadResource.class);

    private IFileRepository fileRepository;

    @Autowired
    public Base64DownloadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
    }

    @GET
    @Path("/{fileId}")
    public void download(
            @Suspended final AsyncResponse asyncResponse,
            @MatrixParam("c") final String clientId,
            @MatrixParam("t") final String token,
            @PathParam("fileId") final String fileId
    ) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                download(fileId, clientId, token, asyncResponse);
            }
        });
        t.setPriority(5);
        t.start();
    }

    @Override
    protected IFileRepository getFileRepository() {
        return fileRepository;
    }

    @Override
    protected Response.ResponseBuilder fetchFile(List<StoreFileInfo> fileInfoList) throws IOException {
        if (fileInfoList.size() != 1) {
            throw new FileDepotWebException("only one file to get in each request.", 400);
        }
        StoreFileInfo fileInfo = fileInfoList.get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4 * 1024);
        OutputStream os = outputStream;
        if (!Common.isBlank(fileInfo.getCipherModel())) {
            os = getDecryptStream(outputStream, fileInfo.getCipherModel(), fileInfo.getSalt());
        }
        fileRepository.fetch(os, fileInfo.getFileId());
        Base64DownloadResp resp = new Base64DownloadResp();
        resp.setFileName(fileInfo.getOriginName());
        resp.setExtName(fileInfo.getExtName());
        resp.setContentType(fileInfo.getContentType());
        resp.setSize(fileInfo.getSize());
        resp.setBase64String(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        Response.ResponseBuilder builder = Response.ok();
        builder.header("Content-Type", "application/json").encoding("UTF-8").entity(resp);
        return builder;
    }
}
