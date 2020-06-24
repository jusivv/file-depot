package org.vvodes.fd.webapp.rest;

import org.apache.commons.io.FilenameUtils;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vvodes.fd.def.intf.IFileRepository;
import org.vvodes.fd.def.pojo.StoreFileInfo;
import org.vvodes.fd.webapp.pojo.Base64UploadReq;
import org.vvodes.fd.webapp.util.AbstractUploadResource;
import org.vvodes.fd.webapp.util.MessageResponseHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Path("attachments/upload/bybase64")
public class Base64UploadResource extends AbstractUploadResource {
    private static Logger log = LoggerFactory.getLogger(Base64UploadResource.class);

    private static Profile profile = Profile.get("config.properties");

    private IFileRepository fileRepository;

    @Autowired
    public Base64UploadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void uploadByBase64(
            final Base64UploadReq req,
            @Suspended final AsyncResponse asyncResponse
    ) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (canWrite(req.getClientId(), req.getToken())) {
                        StoreFileInfo storeFileInfo = new StoreFileInfo();
                        storeFileInfo.setOwner(req.getClientId());
                        storeFileInfo.setOriginName(req.getFileName());
                        storeFileInfo.setExtName(FilenameUtils.getExtension(req.getFileName()));
                        storeFileInfo.setStoreTime(System.currentTimeMillis());
                        storeFileInfo.setSize(req.getFileSize());
                        storeFileInfo.setContentType(req.getContentType());

                        InputStream is = new ByteArrayInputStream(
                                Base64.getDecoder().decode(req.getBase64File()));

                        try {
                            if (req.isEncrypt()) {
                                storeFileInfo.setCipherModel(
                                        profile.getString("file.cipher.model", "aes.v1"));
                                storeFileInfo.setSalt(
                                        Long.toHexString(storeFileInfo.getStoreTime() + storeFileInfo.getSize()));
                                is = getEncryptStream(is, storeFileInfo.getCipherModel(),
                                        storeFileInfo.getSalt());
                            }
                            storeFileInfo.setFileId(fileRepository.store(is, storeFileInfo));
                        } finally {
                            is.close();
                        }
                        asyncResponse.resume(storeFileInfo);
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
