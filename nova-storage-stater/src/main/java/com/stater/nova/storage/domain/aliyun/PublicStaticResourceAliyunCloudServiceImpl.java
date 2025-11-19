package com.stater.nova.storage.domain.aliyun;

import com.stater.nova.storage.domain.ICloudFileService;
import com.stater.nova.storage.enums.FileAccessTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author tql
 * @date 2024/8/30-15:39
 */
@Component
public class PublicStaticResourceAliyunCloudServiceImpl extends AbstractAliyunCloudService implements ICloudFileService {

    @Override
    public FileAccessTypeEnum fileAccessType(CloudType cloudType) {
        return CloudType.OSS == cloudType ? FileAccessTypeEnum.PUBLIC : null;
    }

    @Override
    public String generatePreSignedUrl(String fileObjectKey, Long expiry, String xOssProcess) {
        String url = bucketProperties().getEndpoint() + "/" + fileObjectKey;
        if (StringUtils.hasText(xOssProcess)) {
            url = url + "?x-oss-process=" + xOssProcess;
        }
        return url;
    }
}
