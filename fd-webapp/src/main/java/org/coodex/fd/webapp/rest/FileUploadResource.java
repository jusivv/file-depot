package org.coodex.fd.webapp.rest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.coodex.fd.def.intf.IFileRepository;
import org.coodex.fd.def.pojo.StoreFileInfo;
import org.coodex.fd.webapp.util.AbstractUploadResource;
import org.coodex.fd.webapp.util.MessageResponseHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("attachments/upload/byform")
public class FileUploadResource extends AbstractUploadResource {
    private static Logger log = LoggerFactory.getLogger(FileUploadResource.class);

    private static Profile profile = Profile.get("config.properties");

    private IFileRepository fileRepository;

    @Autowired
    public FileUploadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
    }

    @Path("{clientId}/{tokenId}/{encrypt}")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    public void uploadByForm(
            @Suspended final AsyncResponse asyncResponse,
            @Context final HttpServletRequest request,
            @PathParam("clientId") final String clientId,
            @PathParam("tokenId") final String tokenId,
            @PathParam("encrypt") final int encrypt) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (canWrite(clientId, tokenId)) {
                        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
                        List<FileItem> items = uploadHandler.parseRequest(request);
                        List<StoreFileInfo> fileInfoList = new ArrayList<>();
                        long createTime = System.currentTimeMillis();
                        for (FileItem item : items) {
                            if (!item.isFormField() && !Common.isBlank(item.getName())) {
                                StoreFileInfo storeFileInfo = new StoreFileInfo();
                                storeFileInfo.setOwner(clientId);
                                storeFileInfo.setOriginName(item.getName());
                                storeFileInfo.setExtName(FilenameUtils.getExtension(item.getName()));
                                storeFileInfo.setStoreTime(createTime);
                                storeFileInfo.setSize(item.getSize());
                                storeFileInfo.setContentType(item.getContentType());
                                // store file
                                InputStream is = item.getInputStream();
                                try {
                                    if (encrypt > 0) {
                                        storeFileInfo.setCipherModel(
                                                profile.getString("file.cipher.model", "aes.v1"));
                                        storeFileInfo.setSalt(Long.toHexString(createTime + item.getSize()));
                                        is = getEncryptStream(is, storeFileInfo.getCipherModel(),
                                                storeFileInfo.getSalt());
                                    }
                                    storeFileInfo.setFileId(fileRepository.store(is, storeFileInfo));
                                } finally {
                                    is.close();
                                }
                                fileInfoList.add(storeFileInfo);
                            }
                        }
                        asyncResponse.resume(fileInfoList);
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
}
