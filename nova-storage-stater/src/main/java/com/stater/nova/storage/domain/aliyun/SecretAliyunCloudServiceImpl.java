package com.stater.nova.storage.domain.aliyun;

import com.stater.nova.storage.domain.ICloudFileService;
import com.stater.nova.storage.enums.FileAccessTypeEnum;
import org.springframework.stereotype.Service;

/**
 * 隐私数据（机密数据）
 *
 * @author tql
 * @date 2024/6/14-17:38
 */
@Service
public class SecretAliyunCloudServiceImpl extends AbstractAliyunCloudService implements ICloudFileService {

    @Override
    public FileAccessTypeEnum fileAccessType(CloudType cloudType) {
        return CloudType.OSS == cloudType ? FileAccessTypeEnum.SECRET : null;
    }
}
