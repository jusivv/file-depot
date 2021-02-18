package org.coodex.fd.storage;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.coodex.util.StringHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.coodex.fd.def.intf.IFileRepository;
import org.coodex.fd.def.pojo.StoreFileInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalDiskFileRepository implements IFileRepository {
    private static Logger log = LoggerFactory.getLogger(LocalDiskFileRepository.class);
    private static Profile profile = Profile.get("config.properties");

    /**
     * 生成文件ID
     * @param serverId  文件属主标识
     * @return          文件ID
     */
    private String getId(String serverId) {
        return serverId + "$" + Common.getUUIDStr();
    }

    /**
     * 根据文件ID生成哈希路径
     * @param resourceId    文件ID
     * @return              哈希路径
     */
    private String getPath(String resourceId) {
        int hash = StringHashCode.BKDRHash(resourceId);
        StringBuilder path = new StringBuilder();
        for(int i = 0; i < 4; ++i) {
            String hex = ((hash & 240) == 0?"0":"") + Integer.toHexString(hash & 255);
            hash >>>= 8;
            path = path.append(File.separatorChar).append(hex);
        }
        return path.toString();
    }

    /**
     * 获得文件句柄，同时创建父目录
     * @param fileName  文件全名，包含绝对路径
     * @param force     是否创建文件
     * @return          文件句柄
     * @throws IOException
     */
    private File getFile(String fileName, boolean force) throws IOException {
        File f = new File(fileName);
        if(!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if(!f.exists() && force) {
            f.createNewFile();
        }
        return f;
    }

    @Override
    public String store(InputStream is, StoreFileInfo fileInfo) {
        String resourceId = getId(fileInfo.getOwner());
        String filePath = getFilePath(resourceId);
        storeFile(is, filePath + File.separator + resourceId + ".data");
        fileInfo.setFileId(resourceId);
        setFileInfo(resourceId, filePath, fileInfo);
        return resourceId;
    }

    @Override
    public String store(InputStream is, String owner, String extName) {
        String resourceId = getId(owner);
        storeFile(is, getFilePath(resourceId) + File.separator + resourceId + ".data");
        return resourceId;
    }

    private void storeFile(InputStream is, String dataFile) {
        try {
            File df = getFile(dataFile, true);
            OutputStream os = new FileOutputStream(df);
            try {
                Common.copyStream(is, os);
            } finally {
                os.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public StoreFileInfo getFileInfo(String fileId) {
        String jsonFile = getFilePath(fileId) + File.separator + fileId + ".json";
        try {
            File jf = getFile(jsonFile, false);
            if (!jf.exists()) {
                throw new RuntimeException("file not found, id: " + fileId);
            }
            InputStream fis = new FileInputStream(jf);
            try {
                return JSON.parseObject(fis, StoreFileInfo.class);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String getFilePath(String fileId) {
        String hashPath = getPath(fileId);
        String basePath = profile.getString("file.repository.local.disk.root");
        if (Common.isBlank(basePath) || !Files.isDirectory(Paths.get(basePath))) {
            throw new RuntimeException("local disk file repository root path is illegal");
        }
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }
        return basePath + hashPath;
    }

    private void setFileInfo(String fileId, String filePath, StoreFileInfo fileInfo) {
        if (Common.isBlank(filePath)) {
            filePath = getFilePath(fileId);
        }
        String jsonFile = filePath + File.separator + fileId + ".json";
        try {
            File jf = getFile(jsonFile, true);
            FileWriterWithEncoding writer = new FileWriterWithEncoding(jf, "UTF-8");
            try {
                writer.write(JSON.toJSONString(fileInfo));
                writer.flush();
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFileInfo(String fileId, StoreFileInfo fileInfo) {
        setFileInfo(fileId, null, fileInfo);
    }

    @Override
    public void fetch(OutputStream os, String fileId) {
        String filePath = getFilePath(fileId);
        try {
            File dataFile = getFile(filePath + File.separator + fileId + ".data", false);
            if (!dataFile.exists()) {
                throw new RuntimeException("file not found. id: " + fileId);
            }
            InputStream fis = new FileInputStream(dataFile);
            try {
                // TODO 是否限速
                Common.copyStream(fis, os);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] fetch(String fileId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fetch(baos, fileId);
        return baos.toByteArray();
    }

    @Override
    public void delete(String fileId) {
        String fileName = getFilePath(fileId) + File.separator + fileId;
        try {
            File jf = getFile(fileName + ".json", false);
            if (jf.exists()) {
                jf.delete();
            }
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
        try {
            File df = getFile(fileName + ".data", false);
            if (df.exists()) {
                df.delete();
            }
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
    }
}
