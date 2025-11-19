package com.stater.nova.storage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadExtDTO {

    private Boolean ignoreApplicationName;

    private Boolean ignoreUniqueFileName;

}
