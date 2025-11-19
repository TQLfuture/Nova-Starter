package com.stater.nova.storage.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 18:27
 * @desc:
 */
@Data
@ConfigurationProperties(prefix = "aliyun-oss")
public class AliyunOssProperties {

    /**
     * 是否启用切面类获取url签名
     */
    private boolean enabled = true;
    /**
     * bucket 列表
     */
    private List<BucketProperties> buckets;


    @Data
    public static class BucketProperties {
        /**
         * 存储桶，OSS 中用于存储数据的容器
         */
        private String bucketName;
        /**
         * 桶区域地址网址
         */
        private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        /**
         * bucket的区域
         */
        private String regionName = "cn-hangzhou";
        /**
         * 秘钥id
         */
        private String secretId;
        /**
         * 秘钥key
         */
        private String secretKey;
        /**
         * 访问权限
         */
        private String accessType;

        /**
         * 角色arn
         */
        private String roleArn;
        /**
         * 角色对话名称
         */
        private String roleSessionName;
        /**
         * 角色临时访问stsEndpoint
         */
        private String stsEndpoint = "sts.cn-hangzhou.aliyuncs.com";

        /**
         * 是否使用stsClient
         */
        private Boolean isStsClient;

        /**
         * 启用加速域名
         */
        private String accelerateDomain;
    }
}