package org.coodex.fd.webapp.rest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.coodex.fd.def.intf.IFileRepository;
import org.coodex.fd.def.pojo.CommonFileInfo;
import org.coodex.fd.def.pojo.StoreFileInfo;
import org.coodex.fd.webapp.util.AbstractDownloadResource;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Path("attachments/download")
public class FileDownloadResource extends AbstractDownloadResource {
    private static Logger log = LoggerFactory.getLogger(FileDownloadResource.class);

    private IFileRepository fileRepository;

    @Autowired
    public FileDownloadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
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
    protected Response.ResponseBuilder fetchFile(final List<StoreFileInfo> fileInfoList) throws IOException {
        Response.ResponseBuilder builder = Response.ok();
        if (fileInfoList.size() == 1) {
            StoreFileInfo storeFileInfo = fileInfoList.get(0);
            builder.header("Content-Type", storeFileInfo.getContentType())
                    .header("Content-Disposition",
                            String.format("%s;filename=\"%s\"", getContentDispType(storeFileInfo),
                                    URLEncoder.encode(storeFileInfo.getOriginName(), "UTF-8")));
        } else {
            builder.header("Content-Type", "application/zip")
                    .header("Content-Disposition",
                            String.format("attachment;filename=\"%d.zip\"",
                                    System.currentTimeMillis()));
        }
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                OutputStream outputStream = output;
                ZipArchiveOutputStream zipArchiveOutputStream = null;
                if (fileInfoList.size() > 1) {
                    zipArchiveOutputStream = new ZipArchiveOutputStream(output);
                    outputStream = zipArchiveOutputStream;
                }
                try {
                    int count = 0;
                    for (StoreFileInfo storeFileInfo : fileInfoList) {
                        if (zipArchiveOutputStream != null) {
                            count ++;
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(
                                    String.format("%d-%s", count, storeFileInfo.getOriginName())
                            );
                            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                        }
                        OutputStream os = outputStream;
                        if (!Common.isBlank(storeFileInfo.getCipherModel())) {
                            os = getDecryptStream(outputStream, storeFileInfo.getCipherModel(),
                                    storeFileInfo.getSalt());
                        }
                        fileRepository.fetch(os, storeFileInfo.getFileId());
                        if (zipArchiveOutputStream != null) {
                            zipArchiveOutputStream.closeArchiveEntry();
                        }
                    }
                } finally {
                    outputStream.flush();
                    outputStream.close();
                }
            }
        };
        return builder.entity(output);
    }
}
