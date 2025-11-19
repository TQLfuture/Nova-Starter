package com.stater.nova.storage.model.vo;

import com.stater.nova.storage.model.dto.CloudFileUploadVO;
import com.stater.nova.storage.properties.AliyunOssProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.net.URL;

/**
 * @author tql
 * @date: 2025/11/19
 * @time: 17:49
 * @desc:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliyunOssResult {

    private String bucketName;

    private String contentType;

    /**
     * 文件对象（Object）在存储桶（Bucket）中的唯一标识
     */
    private String fileObjectKey;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private Long fileSize;

    private URL url;

    /**
     * 桶区域地址网址
     */
    private String endpoint;
    /**
     * bucket的区域
     */
    private String regionName;

    /**
     * 文件类型（文件后缀）
     */
    private String fileType;

    public CloudFileUploadVO convertAliyunOssResult(AliyunOssProperties.BucketProperties bucketProperties) {
        String url = this.url.toString();
        String urlDomain = "https://crm-ai-assist.oss-ap-southeast-1-internal.aliyuncs.com";
        if (StringUtils.hasText(bucketProperties.getAccelerateDomain()) && url.startsWith(urlDomain)) {
            url = url.replace(urlDomain, bucketProperties.getAccelerateDomain());
        }
        return CloudFileUploadVO.builder()
                .fileName(this.getFileName())
                .fileObjectKey(this.getFileObjectKey())
                .url(url)
                .fileType(this.getFileType())
                .fileSize(this.getFileSize())
                .build();
    }
}
