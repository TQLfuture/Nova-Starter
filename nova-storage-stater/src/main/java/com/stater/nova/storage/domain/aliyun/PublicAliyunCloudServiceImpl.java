package com.stater.nova.storage.domain.aliyun;

import com.stater.nova.storage.domain.ICloudFileService;
import com.stater.nova.storage.enums.FileAccessTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author tql
 * @date 2024/6/14-17:38
 */
@Service
public class PublicAliyunCloudServiceImpl extends AbstractAliyunCloudService implements ICloudFileService {

    @Override
    public FileAccessTypeEnum fileAccessType(CloudType cloudType) {
        return CloudType.OSS == cloudType ? FileAccessTypeEnum.PRIVATE : null;
    }
}
