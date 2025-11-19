package com.stater.nova.storage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudFileUploadVO {

    private String fileObjectKey;

    private String filePath;

    private String fileName;

    private Long fileSize;

    private String url;

    private String fileType;

}
