package org.coodex.fd.webapp.util;

import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.coodex.fd.def.intf.IAccessController;
import org.coodex.fd.def.intf.IFileCipher;
import org.coodex.fd.def.intf.IFileRepository;
import org.coodex.fd.def.pojo.StoreFileInfo;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDownloadResource {

    private static Logger log = LoggerFactory.getLogger(AbstractDownloadResource.class);
    protected static Profile profile = Profile.get("config.properties");

    /**
     * 获取解密输出流
     * @param os            源输出流
     * @param cipherModel   解密模式
     * @param salt          盐
     * @return              解密输出流
     * @throws IOException
     */
    protected OutputStream getDecryptStream(OutputStream os, String cipherModel, String salt)
            throws IOException {
        // key
        byte[] key = ComponentBuiler.getKey(cipherModel, salt);
        // cipher
        IFileCipher fileCipher = ComponentBuiler.getFileCipher(cipherModel);
        return fileCipher.getDecryptOutputStream(os, key);
    }

    protected void download(String fileId, String clientId, String token, AsyncResponse asyncResponse) {
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
                    StoreFileInfo storeFileInfo = getFileRepository().getFileInfo(fid);
                    if (!readUnlimited && !accessController.inScope(clientId, storeFileInfo.getOwner())) {
                        throw new FileDepotWebException(
                                String.format("forbidden to access file, id: %s, owner: %s, clientId: %s", fid,
                                        storeFileInfo.getOwner(), clientId),
                                403
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
            if (t instanceof FileDepotWebException) {
                FileDepotWebException webException = (FileDepotWebException) t;
                MessageResponseHelper.resume(webException.getStatusCode(), webException.getLocalizedMessage(),
                        asyncResponse);
            } else {
                MessageResponseHelper.resume(500, t.getLocalizedMessage(), asyncResponse);
            }
        }
    }

    protected abstract IFileRepository getFileRepository();

    protected abstract Response.ResponseBuilder fetchFile(final List<StoreFileInfo> fileInfoList) throws IOException;
}
