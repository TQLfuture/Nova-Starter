package com.stater.nova.storage.utils;

import com.stater.nova.storage.domain.ICloudFileService;
import com.stater.nova.storage.enums.FileAccessTypeEnum;
import com.stater.nova.storage.model.dto.CloudFileUploadVO;
import com.stater.nova.storage.model.dto.UploadExtDTO;
import com.stater.nova.storage.model.vo.FileStsVO;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 20:30
 * @desc:
 */
@Component
public class CloudUploadUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 上传文件
     *
     * @param accessType
     * @param file
     * @param filePath   写入的文件路径，路径默认前面会加上 bucketName/applicationName
     * @param fileName
     * @return
     */
    public static CloudFileUploadVO uploadFile(FileAccessTypeEnum accessType, File file, String filePath, String fileName) {
        return findCloudFileService(accessType).uploadFile(file, filePath, fileName, null);
    }

    /**
     * 目前只支持阿里云上传
     *
     * @param accessType
     * @param file
     * @param filePath   写入的文件路径，路径默认前面会加上 bucketName/applicationName
     * @param fileName
     * @return
     */
    public static CloudFileUploadVO uploadFile(FileAccessTypeEnum accessType, MultipartFile file, String filePath, String fileName) {
        return findCloudFileService(accessType).uploadFile(file, filePath, fileName, null);
    }

    /**
     * 目前只支持阿里云上传
     *
     * @param accessType
     * @param file
     * @param filePath   写入的文件路径，路径默认前面会加上 bucketName/applicationName
     * @param fileName
     * @return
     */
    public static CloudFileUploadVO uploadFile(FileAccessTypeEnum accessType, MultipartFile file, String filePath, String fileName, UploadExtDTO ext) {
        return findCloudFileService(accessType).uploadFile(file, filePath, fileName, ext);
    }

    /**
     * @param accessType
     * @param file
     * @param filePath   写入的文件路径，路径默认前面会加上 bucketName/applicationName
     * @param fileName
     * @return 返回的路径类似 bucketName/applicationName(可以通过参数配置)/filePath/xxxxxxxxxxxxxxx.pdf
     */
    public static CloudFileUploadVO uploadFile(FileAccessTypeEnum accessType, InputStream file, String filePath, String fileName) {
        return uploadFile(accessType, file, filePath, fileName, null);
    }

    /**
     * @param accessType
     * @param file
     * @param filePath   写入的文件路径，路径默认前面会加上 bucketName/applicationName
     * @param fileName
     * @param ext
     * @return
     */
    public static CloudFileUploadVO uploadFile(FileAccessTypeEnum accessType, InputStream file, String filePath, String fileName, UploadExtDTO ext) {
        return findCloudFileService(accessType).uploadFile(file, filePath, fileName, ext);
    }

    public static String generateSignedUrl(String fileObjectKey) {
        return generateSignedUrl(fileObjectKey, null);
    }

    public static String generateSignedUrl(String fileObjectKey, Long expiry) {
        return generateSignedUrl(fileObjectKey, expiry, null);
    }

    /**
     * <a href="https://help.aliyun.com/zh/oss/user-guide/overview-17?spm=a2c4g.11186623.0.0.7a364063Zc3x2B">阿里云文档</a>
     *
     * @param fileObjectKey 存储的key
     * @param expiry        redis 过期时间,默认7天
     * @param xOssProcess   对应 x-oss-process
     * @return 加签后的url数据
     */
    public static String generateSignedUrl(String fileObjectKey, Long expiry, String xOssProcess) {
        return findCloudFileService(FileAccessTypeEnum.PRIVATE).generatePreSignedUrl(
                fileObjectKey,
                expiry,
                xOssProcess
        );
    }

    /**
     * 获取临时密钥
     *
     * @param fileExtensionName
     * @param durationSeconds
     * @param sceneCode
     * @return
     */
    public static FileStsVO sts(String fileExtensionName, Long durationSeconds, String sceneCode) {
        return findCloudFileService(FileAccessTypeEnum.PRIVATE).sts(fileExtensionName, durationSeconds, sceneCode);
    }

    /**
     * 默认查找阿里云得实现
     *
     * @param accessType
     * @return
     */
    public static ICloudFileService findCloudFileService(FileAccessTypeEnum accessType) {
        // 默认 查找阿里云
        return findCloudFileService(accessType, ICloudFileService.CloudType.OSS);
    }

    public static ICloudFileService findCloudFileService(FileAccessTypeEnum accessType, ICloudFileService.CloudType cloudType) {
        Map<String, ICloudFileService> beansOfType = applicationContext.getBeansOfType(ICloudFileService.class);
        return beansOfType.values().stream().filter(v -> v.fileAccessType(cloudType) == accessType).findFirst().orElseThrow(() -> new RuntimeException("未找到实例类"));
    }


}
