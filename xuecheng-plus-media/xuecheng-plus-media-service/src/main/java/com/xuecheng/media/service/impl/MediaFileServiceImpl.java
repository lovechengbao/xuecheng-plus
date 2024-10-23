package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient ;
    @Value("${spring.minio.bucket.files}")
    private String filesBucket;
    @Value("${spring.minio.bucket.videofiles}")
    private String videoBucket;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //得到扩展名
        String fileName = uploadFileParamsDto.getFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //得到minio的存储目录
        String defaultFolderPath = getDefaultFolderPath();
        // 得到文件的md5
        String fileMd5 = getFileMd5(new File(localFilePath));
        //得到objectName
        String objectName = defaultFolderPath + fileMd5 + extension;
        // 上传文件到minio
        ObjectWriteResponse response = addMediaFilesToMinio(filesBucket, localFilePath, mimeType, objectName);
        String etag = response.etag();
        if (!Objects.equals(fileMd5, etag)) {
            XueChengPlusException.cast("上传文件失败");
        }
        MediaFileServiceImpl currentProxy = (MediaFileServiceImpl) AopContext.currentProxy();
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, filesBucket, objectName);
        //准备返回的对象
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }

        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5,String bucket,String filePath) {
        // 保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            // 文件id
            mediaFiles.setId(fileMd5);
            // 机构id
            mediaFiles.setCompanyId(companyId);
            // 桶名称
            mediaFiles.setBucket(bucket);
            //filepath
            mediaFiles.setFilePath(filePath);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + filePath);
            // 状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert != 1) {
                log.error("保存数据库失败,bucket:{},objectName:{}",bucket,filePath);
                return null;
            }
        }
        return mediaFiles;
    }


    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;// 通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    public ObjectWriteResponse addMediaFilesToMinio(String bucket, String localFilePath, String mimeType, String objectName) {
        ObjectWriteResponse response = null;
        try {
            response = minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName) // 上传到minio的文件路径和名称
                            .filename(localFilePath)
                            .contentType(mimeType) // 上传文件的类型
                            .build());
            log.debug("上传文件成功,bucket:{},objectName:{}", bucket, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return response;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }


    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
