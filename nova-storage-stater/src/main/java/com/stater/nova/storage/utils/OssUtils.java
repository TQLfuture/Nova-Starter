package com.stater.nova.storage.utils;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.*;
import com.stater.nova.storage.properties.AliyunOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 19:22
 * @desc:
 */
@Slf4j
public class OssUtils {

    private static final SnowflakeGenerator GENERATOR = new SnowflakeGenerator();
    private static final String SIGN_CACHE_KEY = "SIGN_CACHE_KEY:";
    public static final String FILE_PATH_SPLIT = "/";
    public static final String FILE_NAME_SPLIT = ".";


    /**
     * 创建连接
     *
     * @return
     */
    public static OSS connect(String secretId, String secretKey, String endpoint, String regionName, String securityToken) {
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(secretId, secretKey, securityToken);
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSupportCname(true);
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(regionName)
                .build();
    }

    /**
     * 创建桶
     *
     * @param ossClient
     */
    public static void createBucket(OSS ossClient, String bucket) {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        //        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写资源组ID。如果不填写资源组ID，则创建的Bucket属于默认资源组。
        //String rsId = "rg-aek27tc****";
        try {
            // 创建CreateBucketRequest对象。
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
            // 如果创建存储空间的同时需要指定存储类型、存储空间的读写权限、数据容灾类型, 请参考如下代码。
            // 此处以设置存储空间的存储类型为标准存储为例介绍。
            //createBucketRequest.setStorageClass(StorageClass.Standard);
            // 数据容灾类型默认为本地冗余存储，即DataRedundancyType.LRS。如果需要设置数据容灾类型为同城冗余存储，请设置为DataRedundancyType.ZRS。
            //createBucketRequest.setDataRedundancyType(DataRedundancyType.ZRS);
            // 设置存储空间读写权限为公共读，默认为私有。
            //createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
            // 在支持资源组的地域创建Bucket时，您可以为Bucket配置资源组。
            //createBucketRequest.setResourceGroupId(rsId);
            // 创建存储空间。
            ossClient.createBucket(createBucketRequest);
        } catch (Exception e) {
            log.info("创建Bucket失败,{}", e);
        }
    }

    /**
     * 查询桶列表
     *
     * @param ossClient
     */
    public static List<Bucket> getBucketList(OSS ossClient) {
        List<Bucket> buckets = null;
        try {
            // 列举当前账号所有地域下的存储空间。
            buckets = ossClient.listBuckets();
        } catch (Exception e) {
            System.out.println(e);
        }
        return buckets;
    }

    /**
     * 桶是否存在
     *
     * @param bucketName
     * @return
     */
    public static Boolean bucketExist(OSS ossClient, String bucketName) {
        boolean exists = false;
        try {
            // 判断存储空间examplebucket是否存在。如果返回值为true，则存储空间存在，如果返回值为false，则存储空间不存在。
            exists = ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.info("查询桶是否存在失败,{}", e);
        }
        return exists;
    }


    /**
     * 桶是否存在
     *
     * @param bucketName
     * @return
     */
    public static void checkAndCreateBucket(OSS ossClient, String bucketName) {
        if (!bucketExist(ossClient, bucketName)) {
            createBucket(ossClient, bucketName);
        }
    }

    /**
     * 上传对象
     *
     * @param ossClient
     */
    public static PutObjectResult uploadFile(OSS ossClient, String bucketName, String filePath, String objectName) {
        PutObjectResult result = null;
        try {
            InputStream inputStream = new FileInputStream(filePath);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求。
            result = ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.info("上传文件失败,{}", e);
        }
        return result;
    }

    /**
     * 上传文件流
     *
     * @param ossClient
     */
    public static PutObjectResult uploadInputStream(OSS ossClient, String bucketName, String key, InputStream inputStream, String contentType, Long contentLength) {
        PutObjectResult result = null;
        // 方法2 从输入流上传(需提前告知输入流的长度, 否则可能导致 oom)
        ObjectMetadata metadata = new ObjectMetadata();
        // 设置输入流长度为500
        if (Objects.nonNull(contentLength)) {
            metadata.setContentLength(contentLength);
        }
        // 设置 Content type, 如果没设置，则默认是 application/octet-stream
        if (StringUtils.hasText(contentType)) {
            // 跟据传入contentType设置，如果传入是image/jpeg，则图片可以直接预览
            metadata.setContentType(contentType);
        }
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            result = ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("阿里云上传文件失败,{},{}", bucketName, key, e);
        }
        return result;
    }


    /**
     * 流式下载
     *
     * @param ossClient
     * @param bucketName
     * @param key
     */
    public static <T extends InputStream> void downloadStream(OSS ossClient, String bucketName, String key, Consumer<T> function) {
        try {
            // ossObject包含文件所在的存储空间名称、文件名称、文件元数据以及一个输入流。
            int limitSpeed = 100 * 1024 * 8;
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            //在文件下载的时候实现限速下载
            getObjectRequest.setTrafficLimit(limitSpeed);
            OSSObject object = ossClient.getObject(getObjectRequest);
            InputStream objectContent = object.getObjectContent();
            function.accept((T) objectContent);
            objectContent.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * 下载到本地本地
     *
     * @param ossClient
     * @param bucketName
     * @param objectName
     * @param pathName
     */

    public static void downLoadFile(OSS ossClient, String bucketName, String objectName, String pathName) {
        try {
            // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
            // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(pathName));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * 删除文件
     *
     * @param ossClient
     * @param bucketName
     * @param objectName
     */
    public static void delFile(OSS ossClient, String bucketName, String objectName) {
        ossClient.deleteObject(bucketName, objectName);
    }

    /**
     * 生成上传文件的的url(支持下载)
     */
    public static URL generateUploadFileUrl(OSS ossClient, String bucketName, String key) {
        URL signedUrl = null;
        try {
            // 指定生成的签名URL过期时间，单位为毫秒。本示例以设置过期时间为1小时为例。
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000L);
            // 生成签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);
            // 设置过期时间。
            request.setExpiration(expiration);
            // 通过HTTP GET请求生成签名URL。
            signedUrl = ossClient.generatePresignedUrl(request);
            // 打印签名URL。
            log.info("signed url for getObject: " + signedUrl);
        } catch (Exception e) {
            log.error("生成上传文件的的url失败", e);
            throw new RuntimeException("生成上传文件的的url失败" + e.getMessage());
        }
        return signedUrl;
    }

    /**
     * 上传管理元数据的文件（通过对元数据的设置实现上传的文件生成的URL实现预览）
     */
    public static void uploadMedataManageFile(OSS ossClient, String bucketName, String objectName, InputStream inputStream) {
        try {
            //            getcontentType(file.getName()).substring(file.getName().lastIndexOf("."))
            // 创建上传文件的元数据。
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType("image/jpg");
            meta.setObjectAcl(CannedAccessControlList.PublicRead);
            meta.setContentDisposition("inline");
/*
            // 设置内容被下载时的名称。
            meta.setContentDisposition("attachment; filename=\"DownloadFilename\"");
            // 设置内容被下载时网页的缓存行为。
            meta.setCacheControl("Download Action");
            // 设置缓存过期时间，格式是格林威治时间（GMT）。
            meta.setExpirationTime(DateUtil.parseIso8601Date("2022-10-12T00:00:00.000Z"));
            // 设置内容被下载时的编码格式。
            meta.setContentEncoding("gzip");
            // 设置Header。
            meta.setHeader("yourHeader", "yourHeaderValue");*/
            // 上传文件。
            ossClient.putObject(bucketName, objectName, inputStream, meta);
        } catch (Exception e) {
            System.out.println(e);
        }

    }


    public static String getBucketFileUniqueKey(String bucketName, String filePath, String fileNameOrExtensionName) {
        String randomStr = GENERATOR.next().toString();
        randomStr = getMD5Str(System.currentTimeMillis() + randomStr);
        filePath = filePath.startsWith(FILE_PATH_SPLIT) ? filePath.substring(filePath.indexOf(FILE_PATH_SPLIT) + 1) : filePath;
        filePath = filePath.endsWith(FILE_PATH_SPLIT) ? filePath.substring(0, filePath.lastIndexOf(FILE_PATH_SPLIT)) : filePath;
        String[] split = fileNameOrExtensionName.split("\\" + FILE_NAME_SPLIT);
        String extension = split.length == 0 ? fileNameOrExtensionName : split[split.length - 1];
        return bucketName + FILE_PATH_SPLIT + filePath + FILE_PATH_SPLIT + randomStr + FILE_NAME_SPLIT + extension;
    }


    public static String generatePreSignedUrlForDownload(OSS ossClient, String bucketName, String key, String contentType, Long expiry) {
        String urlStr = "";
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);
        if (expiry == null) {
            expiry = 30L * 60L * 1000L;
        }

        //阿里侧的过期时间加5s
        expiry += 5 * 1000L;
        Date expirationDate = new Date(System.currentTimeMillis() + expiry);
        // 设置下载时返回的 http 头
        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
        if (StringUtils.hasText(contentType)) {
            responseHeaders.setContentType(contentType);
        }
        request.setExpiration(expirationDate);
        request.setResponseHeaders(responseHeaders);
        URL url = ossClient.generatePresignedUrl(request);
        return url == null ? null : url.toString();
    }

    public static TreeMap<String, Object> getOssConfig(AliyunOssProperties.BucketProperties bucketProperties, Long durationSeconds) {
        TreeMap<String, Object> config = new TreeMap<>();
        // 替换为您的云 api 密钥 SecretId
        config.put("secretId", bucketProperties.getSecretId());
        // 替换为您的云 api 密钥 SecretKey
        config.put("secretKey", bucketProperties.getSecretKey());
        // 临时密钥有效时长，单位是秒，30分钟
        config.put("durationSeconds", Objects.isNull(durationSeconds) ? 1800L : durationSeconds);
        config.put("bucketName", bucketProperties.getBucketName());
        config.put("region", bucketProperties.getRegionName());
        //角色的ARN 本次会话的名称
        config.put("roleArn", bucketProperties.getRoleArn());
        //本次会话的名称
        config.put("roleSessionName", bucketProperties.getRoleSessionName());
        config.put("endpoint", bucketProperties.getStsEndpoint());
        return config;
    }

    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String s = String.format("%02x", b);
                sb.append(s);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <a href='https://help.aliyun.com/zh/oss/developer-reference/img-2?spm=a2c4g.11186623.0.0.74e374e9TPHQa4#concept-agt-jgc-kfb'>接口文档</a>
     * <p>
     * 出现巨坑：CDN会默认把x-oss-process参数剔除，需要在CDN配置。文档<a href='https://help.aliyun.com/zh/oss/user-guide/faq-2'>经过CDN加速后图片处理没有效果</a>
     *
     * @param ossClient
     * @param bucketName
     * @param key
     * @param contentType
     * @param expiry
     * @param xOssProcess
     * @return
     */
    public static String ossGenerateUrl(OSS ossClient, String bucketName, String key, String contentType, Long expiry, String xOssProcess) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);
        // 过期时间官方文档：https://help.aliyun.com/zh/oss/user-guide/whether-you-can-set-the-url-of-a-private-object-to-not-expire?spm=a2c4g.11186623.0.0.12072377SrQifH
        //阿里侧的过期时间加5s
        long time = expiry == null ? 7 * 24 * 60L * 60L * 1000L + 5 * 1000L : expiry + 5 * 1000L;
        Date expirationDate = new Date(System.currentTimeMillis() + time);
        // 设置下载时返回的 http 头
        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
        if (StringUtils.hasText(contentType)) {
            responseHeaders.setContentType(contentType);
        }
        request.setExpiration(expirationDate);
        request.setResponseHeaders(responseHeaders);
        request.setProcess(xOssProcess);
        URL url = ossClient.generatePresignedUrl(request);
        return url == null ? null : url.toString();
    }
}
