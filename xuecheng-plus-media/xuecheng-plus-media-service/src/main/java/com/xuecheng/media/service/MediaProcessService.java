package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;


public interface MediaProcessService {

    List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count);

    boolean startTask(Long id);

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
