package com.xuecheng.media.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileParamsDto {
    public String filename;
    public String fileType;
    public Long fileSize;
    // 标签
    public String tags;
    //上传人
    public String username;
    // 备注
    public String remark;
}
