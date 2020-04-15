package org.vvodes.fd.webapp.rest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vvodes.fd.def.intf.IAccessController;
import org.vvodes.fd.def.intf.IFileCipher;
import org.vvodes.fd.def.intf.IFileRepository;
import org.vvodes.fd.def.pojo.StoreFileInfo;
import org.vvodes.fd.webapp.util.ComponentBuiler;
import org.vvodes.fd.webapp.util.MessageResponseHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("attachments/upload/byform")
public class FileUploadResource {
    private static Logger log = LoggerFactory.getLogger(FileUploadResource.class);

    private static Profile profile = Profile.get("config.properties");

    private IFileRepository fileRepository;

    @Autowired
    public FileUploadResource(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
        log.debug("resource {} loaded.", this.getClass().getSimpleName());
    }

    /**
     * 判断是否有写权限
     * @param clientId  终端ID
     * @param token     令牌
     * @return          是否允许写
     */
    private boolean canWrite(String clientId, String token) {
        IAccessController accessControler = ComponentBuiler.getAccessController(clientId);
        return accessControler.canWrite(clientId, token);
    }

    private InputStream getEncryptStream(InputStream is, String cipherModel, String salt)
            throws IOException {
        // key
        byte[] key = ComponentBuiler.getKey(cipherModel, salt);
        // cipher
        IFileCipher fileCipher = ComponentBuiler.getFileCipher(cipherModel);
        return fileCipher.getEncryptInputStream(is, key);
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
