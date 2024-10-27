package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.ObjectWriteResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class VideoTask {
    @Autowired
    private MediaProcessService mediaProcessService;
    @Autowired
    private MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;
    @Value("${spring.minio.bucket.videofiles}")
    private String videoBucket;

    /**
     * 2、分片广播任务  视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        // cpu核心数
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // 查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardTotal, shardIndex, availableProcessors);
        // 获取任务数量
        int size = mediaProcessList.size();
        // 开启任务 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // mediaProcessService.startTask()
        // 执行视频转码
        // 使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(new Consumer<MediaProcess>() {
            @Override
            public void accept(MediaProcess mediaProcess) {
                executorService.execute(() -> {
                    try {
                        // 任务执行逻辑
                        Long taskId = mediaProcess.getId();
                        boolean success = mediaProcessService.startTask(taskId);
                        if (!success) {
                            log.error("抢占任务失败,任务id:{}", taskId);
                        }

                        String bucket = mediaProcess.getBucket();
                        String filePath = mediaProcess.getFilePath();
                        String fileId = mediaProcess.getFileId();

                        // 源avi视频的路径
                        File sourceFile = mediaFileService.downloadFile(bucket, filePath);
                        if (sourceFile == null) {
                            log.debug("下载文件失败,任务id:{},bucket:{},objectName:{}", taskId, bucket, filePath);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载任务失败");
                            return;
                        }
                        String video_path = sourceFile.getAbsolutePath();
                        // 转换后mp4文件的名称
                        String mp4_name = fileId + ".mp4";
                        // 转换后mp4文件的路径
                        File mp4File = null;
                        try {
                            mp4File = File.createTempFile("minio", ".mp4");
                        } catch (IOException e) {
                            log.debug("创建临时文件失败,异常信息:{}", e.getMessage());
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, e.getMessage());
                            return;
                        }
                        String mp4_path = mp4File.getAbsolutePath();
                        // 创建工具类对象
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4_path);
                        // 开始视频转换，成功将返回success
                        String result = videoUtil.generateMp4();
                        if (!"success".equals(result)) {
                            // 转换失败
                            log.debug("视频转码失败,失败原因:{},任务id:{},bucket:{},objectName:{}", result, taskId, bucket, filePath);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转换失败");
                            return;
                        }
                        String url = getFilePath(fileId, ".mp4");
                        ObjectWriteResponse response = mediaFileService.addMediaFilesToMinio(bucket, mp4_path, "video/mp4", url);
                        if (response.etag().isEmpty()) {
                            log.debug("上传视频失败,失败原因:{},任务id:{},bucket:{},objectName:{}", result, taskId, bucket, filePath);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传视频失败");
                            return;
                        }
                        // 上传成功
                        mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
        });

        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    @NotNull
    private static String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + fileExt;
    }


}
