package com.stater.nova.storage.domain;

import com.stater.nova.storage.enums.FileAccessTypeEnum;
import com.stater.nova.storage.model.dto.CloudFileUploadVO;
import com.stater.nova.storage.model.dto.UploadExtDTO;
import com.stater.nova.storage.model.vo.FileStsVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author tql
 * @date: 2025/11/19
 * @time: 17:12
 * @desc:
 */
public interface ICloudFileService {

    enum CloudType {
        OSS, COS;
    }

    /**
     * 文件访问类型
     *
     * @return
     */
    FileAccessTypeEnum fileAccessType(CloudType cloudType);

    /**
     * 上传文件
     *
     * @param file
     * @param filePath
     * @param fileName
     * @param ext      扩展数据，可为空
     * @return
     */
    CloudFileUploadVO uploadFile(File file, String filePath, String fileName, UploadExtDTO ext);

    /**
     * 上传文件
     *
     * @param file
     * @param filePath
     * @param fileName
     * @param ext      扩展数据
     * @return
     */
    CloudFileUploadVO uploadFile(InputStream file, String filePath, String fileName, UploadExtDTO ext);

    /**
     * 上传文件
     *
     * @param file
     * @param filePath
     * @param fileName
     * @param ext
     * @return
     */
    CloudFileUploadVO uploadFile(MultipartFile file, String filePath, String fileName, UploadExtDTO ext);

    /**
     * 删除文件
     *
     * @param objectKey
     * @return
     */
    boolean delFile(String objectKey);

    /**
     * 流式下载
     *
     * @param fileObjectKey
     * @param function
     */
    void download(String fileObjectKey, Consumer<InputStream> function);

    /**
     * 获取签名的链接
     *
     * @param fileObjectKey
     * @return
     */
    default String generatePreSignedUrl(String fileObjectKey) {
        return generatePreSignedUrl(fileObjectKey, null);
    }


    /**
     * 获取签名的链接
     *
     * @param fileObjectKey
     * @param expiry
     * @return
     */
    default String generatePreSignedUrl(String fileObjectKey, Long expiry) {
        return generatePreSignedUrl(fileObjectKey, expiry, null);
    }

    /**
     * 获取签名的链接
     *
     * @param fileObjectKey
     * @param expiry
     * @param xOssProcess
     * @return
     */
    String generatePreSignedUrl(String fileObjectKey, Long expiry, String xOssProcess);

    /**
     * 获取临时密钥
     *
     * @return
     */
    FileStsVO sts(String fileExtensionName, Long durationSeconds, String sceneCode);


    boolean createBucket(String bucketName);
}
