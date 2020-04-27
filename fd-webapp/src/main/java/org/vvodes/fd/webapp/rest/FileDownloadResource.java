package org.vvodes.fd.webapp.rest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
import org.vvodes.fd.webapp.util.ComponentBuiler;
import org.vvodes.fd.webapp.util.MessageResponseHelper;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
                    // whether or read unlimited
                    boolean readUnlimited = profile.getBool("read.unlimited", false);
                    // get access controller
                    IAccessController accessController = ComponentBuiler.getAccessController(clientId);
                    // authenticate
                    if (readUnlimited || accessController.canRead(clientId, token, fileId)) {
                        // list file info
                        String[] fids = fileId.split(",");
                        List<StoreFileInfo> fileInfoList = new ArrayList<>();
                        for (String fid : fids) {
                            StoreFileInfo storeFileInfo = fileRepository.getFileInfo(fid);
                            if (!readUnlimited && !accessController.inScope(fid, storeFileInfo.getOwner())) {
                                throw new RuntimeException(
                                        String.format("forbidden to access file, id: %s, owner: %s", fid,
                                                storeFileInfo.getOwner())
                                );
                            }
                            fileInfoList.add(storeFileInfo);
                        }
                        // fetch file
                        asyncResponse.resume(fetchFile(fileInfoList).build());
                    } else {
                        MessageResponseHelper.resume(403, "Access Forbidden", asyncResponse);
                    }
                } catch (Throwable t) {
                    log.error(t.getLocalizedMessage(), t);
                    MessageResponseHelper.resume(500, t.getLocalizedMessage(), asyncResponse);
                }
            }
        });
        t.setPriority(5);
        t.start();
    }

    private Response.ResponseBuilder fetchFile(final List<StoreFileInfo> fileInfoList) throws IOException {
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
