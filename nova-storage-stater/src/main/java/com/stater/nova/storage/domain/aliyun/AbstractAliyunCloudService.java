package com.stater.nova.storage.domain.aliyun;


import cn.hutool.core.util.BooleanUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.starter.nova.common.code.BadRequest;
import com.starter.nova.common.code.InternalServerError;
import com.starter.nova.common.exception.BusinessException;
import com.stater.nova.storage.domain.ICloudFileService;
import com.stater.nova.storage.enums.FileAccessTypeEnum;
import com.stater.nova.storage.model.dto.CloudFileUploadVO;
import com.stater.nova.storage.model.dto.UploadExtDTO;
import com.stater.nova.storage.model.vo.AliyunOssResult;
import com.stater.nova.storage.model.vo.FileStsVO;
import com.stater.nova.storage.plugin.DefaultStoreCachePlugin;
import com.stater.nova.storage.plugin.IStoreCachePlugin;
import com.stater.nova.storage.properties.AliyunOssProperties;
import com.stater.nova.storage.utils.OssStsClient;
import com.stater.nova.storage.utils.OssUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author tql
 * @date 2024/6/14-17:37
 */
@Slf4j
public abstract class AbstractAliyunCloudService implements ICloudFileService, DisposableBean {

    @Autowired
    protected AliyunOssProperties aliyunOssProperties;

    private volatile OSS ossClient = null;

    @Autowired(required = false)
    private IStoreCachePlugin storeCachePlugin;

    @Value("${spring.application.name}")
    private String applicationName;

    public FileAccessTypeEnum fileAccessType() {
        return fileAccessType(CloudType.OSS);
    }

    /**
     * 获取阿里云得配置
     *
     */
    protected AliyunOssProperties.BucketProperties bucketProperties() {
        AliyunOssProperties.BucketProperties bucketProperties = aliyunOssProperties.getBuckets().stream()
                .filter(v -> v.getAccessType().equals(fileAccessType().name())).findFirst()
                .orElseThrow(() -> new BusinessException(new BadRequest("阿里云OSS配置不存在")));
        // todo check properties 配置
        Assert.notNull(bucketProperties, "阿里云OSS配置不存在");
        Assert.notNull(bucketProperties.getBucketName(), "阿里云 bucketName 配置不存在");
        Assert.notNull(bucketProperties.getSecretId(), "阿里云 secretId 配置不存在");
        Assert.notNull(bucketProperties.getSecretKey(), "阿里云 secretKey 配置不存在");
        return bucketProperties;
    }

    @Override
    public CloudFileUploadVO uploadFile(File file, String filePath, String fileName, UploadExtDTO ext) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            AliyunOssResult aliyunOssResult = ossUploadFile(fileInputStream, OssUploadInfo.builder().fileName(fileName).
                    filePath(filePath).ignoreApplicationName(ext != null && ext.getIgnoreApplicationName()).build());
            return aliyunOssResult.convertAliyunOssResult(bucketProperties());
        } catch (FileNotFoundException e) {
            log.error("文件不存在", e);
            throw new BusinessException(new InternalServerError("uploadFile:" + e.getMessage()));
        }
    }

    @Override
    public CloudFileUploadVO uploadFile(InputStream file, String filePath, String fileName, UploadExtDTO ext) {
        AliyunOssResult aliyunOssResult = ossUploadFile(file, OssUploadInfo.builder()
                .fileName(fileName)
                .filePath(filePath)
                .ignoreApplicationName(ext != null && ext.getIgnoreApplicationName())
                .ignoreUniqueFileName(ext != null && ext.getIgnoreUniqueFileName())
                .build());
        return aliyunOssResult.convertAliyunOssResult(bucketProperties());
    }

    @Override
    public CloudFileUploadVO uploadFile(MultipartFile file, String filePath, String fileName, UploadExtDTO ext) {
        try {
            AliyunOssResult aliyunOssResult = ossUploadFile(file, OssUploadInfo.builder()
                    .fileName(fileName)
                    .filePath(filePath)
                    .ignoreApplicationName(ext != null && ext.getIgnoreApplicationName())
                    .ignoreUniqueFileName(ext != null && ext.getIgnoreUniqueFileName())
                    .build());
            return aliyunOssResult.convertAliyunOssResult(bucketProperties());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(new InternalServerError("uploadFile:" + e.getMessage()));
        }
    }

    @Override
    public void destroy() throws Exception {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean delFile(String objectKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String generatePreSignedUrl(String fileObjectKey, Long expiry, String xOssProcess) {
        GenerateParamHolder holder = GenerateParamHolder.of(getConnect(), bucketProperties().getBucketName());
        holder.fileObjectKey = fileObjectKey;
        holder.expiry = expiry;
        holder.storeCachePlugin = storeCachePlugin == null ? DefaultStoreCachePlugin.DEFAULT_STORE_CACHE_PLUGIN : storeCachePlugin;
        holder.ossProcess = !StringUtils.hasText(xOssProcess) ? null : xOssProcess;
        return holder.generateUlrWithCache();
    }

    @Override
    public void download(String fileObjectKey, Consumer<InputStream> function) {
        AliyunOssProperties.BucketProperties bucketProperties = bucketProperties();
        OssUtils.downloadStream(getConnect(), bucketProperties.getBucketName(), fileObjectKey, function);
    }

    @Override
    public FileStsVO sts(String fileExtensionName, Long durationSeconds, String sceneCode) {
        AliyunOssProperties.BucketProperties bucketProperties = bucketProperties();
        // 判断key是否在当前存储桶唯一，获取一个唯一的key
        String appName = applicationName;
        FileStsVO sts = sts(durationSeconds);
        if (sts == null) {
            return null;
        }
        String key = OssUtils.getBucketFileUniqueKey(bucketProperties.getBucketName() + "/" + appName, "public/file", fileExtensionName);
        sts.setKeyPrefix(key);
        sts.setObjectKey(key);
        return sts;
    }

    @Override
    public boolean createBucket(String bucketName) {
        OssUtils.checkAndCreateBucket(this.getConnect(), bucketName);
        return true;
    }

    private FileStsVO sts(Long durationSeconds) {
        AliyunOssProperties.BucketProperties bucketProperties = bucketProperties();
        // 判断key是否在当前存储桶唯一，获取一个唯一的key
        String appName = applicationName;
        try {
            TreeMap<String, Object> config = OssUtils.getOssConfig(bucketProperties, durationSeconds);
            AssumeRoleResponse temporaryAccessCredentials = OssStsClient.getTemporaryAccessCredentials(config);
            AssumeRoleResponse.Credentials credentials = temporaryAccessCredentials.getCredentials();
            // String endPointAliyun = "https://oss-" + bucketProperties.getRegionName() + ".aliyuncs.com";
            Function<String, Long> fun = (expiration) -> expiration == null ?
                    0L : LocalDateTime.ofInstant(Instant.parse(expiration), ZoneOffset.UTC)
                    .toEpochSecond(ZoneOffset.UTC);
            return FileStsVO.builder()
                    .endPoint(bucketProperties.getEndpoint())
                    .cName(true)
                    .tmpSecretId(credentials.getAccessKeyId())
                    .tmpSecretKey(credentials.getAccessKeySecret())
                    .expiredTime(fun.apply(credentials.getExpiration()))
                    .keyPrefix(null).objectKey(null)
                    .bucketName(bucketProperties.getBucketName())
                    .regionName(bucketProperties.getRegionName())
                    .sessionToken(credentials.getSecurityToken()).build();
        } catch (Exception e) {
            log.error("=== ossSts err ===", e);
            throw new BusinessException("获取临时凭证失败:" + e.getMessage());
        }
    }

    protected AliyunOssResult ossUploadFile(MultipartFile file, OssUploadInfo param) throws IOException {
        // 获取文件的原始名称
        String originalFilename =
                ObjectUtils.isEmpty(param.getFileName()) ? file.getOriginalFilename() : param.getFileName();
        param.setFileName(originalFilename);
        return uploadAliyun(file.getInputStream(), param, file.getContentType(), file.getSize());
    }

    /**
     * 数据上传
     *
     * @param inputStream 输入流
     * @param param       上传参数
     * @return 上传结果
     */
    protected AliyunOssResult ossUploadFile(InputStream inputStream, OssUploadInfo param) {
        checkInfo(param);
        return uploadAliyun(inputStream, param, null, null);
    }

    private AliyunOssResult uploadAliyun(InputStream file, OssUploadInfo param, String contentType, Long contentLength) {
        String originalFilename = param.fileName;
        String ossFilePath = param.getFilePath();
        AliyunOssProperties.BucketProperties bucketProperties = bucketProperties();
        String bucketName = bucketProperties.getBucketName();
        String extension = StringUtils.hasText(originalFilename) && originalFilename.lastIndexOf(".") >= 0 ?
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1) : null;
        OSS ossClient = getConnect();
        // 判断key是否在当前存储桶唯一，获取一个唯一的key
        String appName = Boolean.TRUE.equals(param.getIgnoreApplicationName()) ? "" : "/" + applicationName;
        String key = OssUtils.getBucketFileUniqueKey(bucketName + appName, ossFilePath, originalFilename);
        if (Boolean.TRUE.equals(param.getIgnoreUniqueFileName())) {
            // 直接生成key，不用随机文件名，即可以上传到指定文件对象
            key = bucketName + OssUtils.FILE_PATH_SPLIT + ossFilePath + OssUtils.FILE_PATH_SPLIT + originalFilename;
        }
        PutObjectResult putObjectResult = OssUtils.uploadInputStream(ossClient, bucketName, key, file, contentType, contentLength);
        if (putObjectResult != null) {
            URL url = OssUtils.generateUploadFileUrl(ossClient, bucketName, key);
            return AliyunOssResult.builder().fileObjectKey(key).bucketName(bucketName)
                    .fileSize(contentLength).contentType(contentType)
                    .fileName(originalFilename).url(url).fileType(extension)
                    .endpoint(bucketProperties.getEndpoint()).regionName(bucketProperties.getRegionName())
                    .build();
        }
        return null;
    }

    /**
     * OSSClient是线程安全的，允许多线程访问同一实例。您可以结合业务需求，复用同一个OSSClient实例，也可以创建多个OSSClient实例，分别使用。
     * 获取连接
     * <a href="https://help.aliyun.com/zh/oss/developer-reference/initialization-3?spm=a2c4g.11186623.0.i3">Java初始化</a>
     *
     * @return
     */
    private OSS getConnect() {
        AliyunOssProperties.BucketProperties bucketProperties = bucketProperties();
        Assert.notNull(bucketProperties, "the bucketProperties  cannot not be null");
        if (BooleanUtil.isTrue(bucketProperties.getIsStsClient())) {
            FileStsVO sts = sts(null);
            // 开始上传
            return OssUtils.connect(sts.getTmpSecretId(), sts.getTmpSecretKey(), sts.getEndPoint(), sts.getRegionName(), sts.getSessionToken());
        }
        if (this.ossClient == null) {
            synchronized (OSSClient.class) {
                if (this.ossClient == null) {
                    this.ossClient = OssUtils.connect(bucketProperties.getSecretId(),
                            bucketProperties.getSecretKey(), bucketProperties.getEndpoint(), bucketProperties.getRegionName(), null);
                }
            }
        }
        return this.ossClient;
    }

    private void checkInfo(OssUploadInfo uploadInfo) {

    }

    private static class GenerateParamHolder {
        private static final String SIGN_CACHE_KEY = "SIGN_CACHE_KEY:";
        private IStoreCachePlugin storeCachePlugin;
        private OSS ossClient;
        private String bucketName;
        private String fileObjectKey;
        private String contentType;
        private Long expiry;

        /**
         * oss 图片处理的参数
         */
        private String ossProcess;

        public static GenerateParamHolder of(OSS ossClient, String bucketName) {
            GenerateParamHolder holder = new GenerateParamHolder();
            holder.bucketName = bucketName;
            holder.ossClient = ossClient;
            return holder;
        }

        /**
         * 数据进行缓存
         *
         * @param signedUrl 缓存的objectKey
         */
        private void cache(String signedUrl) {
            if (Objects.isNull(this.storeCachePlugin) || !StringUtils.hasText(this.fileObjectKey) || !StringUtils.hasText(signedUrl)) {
                return;
            }
            // 阿里云文件缓存时间 https://help.aliyun.com/zh/oss/user-guide/whether-you-can-set-the-url-of-a-private-object-to-not-expire?spm=a2c4g.11186623.0.0.12072377SrQifH
            // 默认延迟到7天
            String key = cacheKey();
            try {
                // 这里有个bug 如果两个请求设置的缓存时间不一致，就会导致有一方的缓存设置失败
                this.storeCachePlugin.cache(key, signedUrl);
            } catch (Exception e) {
                log.error("AbstractAliyunCloudService,cache,key:{}", key, e);
            }
        }

        /**
         * 生成 oss url
         *
         * @return
         */
        public String generateUrl() {
            return OssUtils.ossGenerateUrl(this.ossClient, this.bucketName, this.fileObjectKey, this.contentType, this.expiry, this.ossProcess);
        }

        /**
         * 从缓存获取数据
         *
         * @return
         */
        public String getUrlFromCache() {
            if (!StringUtils.hasText(this.fileObjectKey)) {
                return null;
            }
            String key = cacheKey();
            try {
                return this.storeCachePlugin.fromCache(key);
            } catch (Exception e) {
                log.error("getUrlFromCache,{}", key, e);
                return null;
            }
        }

        /**
         * 生成url与缓存一起使用
         *
         * @return
         */
        public String generateUlrWithCache() {
            String s = getUrlFromCache();
            if (s == null) {
                s = generateUrl();
                cache(s);
            }
            return s;
        }

        public String cacheKey() {
            String prefix = this.ossProcess == null ? this.storeCachePlugin.cachePrefix(this.fileObjectKey) : this.storeCachePlugin.cachePrefix(this.fileObjectKey) + this.ossProcess;
            return prefix + this.fileObjectKey;
        }
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class OssUploadInfo {

        private String fileName;

        private String filePath;

        private Boolean ignoreApplicationName;

        private Boolean ignoreUniqueFileName;

    }
}
