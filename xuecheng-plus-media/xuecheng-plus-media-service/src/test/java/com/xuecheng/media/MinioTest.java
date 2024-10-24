package com.xuecheng.media;


import com.alibaba.nacos.common.utils.MD5Utils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import okhttp3.Headers;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.substring;

public class MinioTest {
    private MinioClient minioClient;

    @BeforeEach
    public void setUp() throws Exception {
        minioClient =
                MinioClient.builder()
                        .endpoint("http://47.99.144.74:9000")
                        .credentials("fileadmin", "fileadmin")
                        .build();
    }


    @Test
    void upLoad() throws Exception {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }



        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("mediafiles")
                        .object("test/风景1.png") // 上传到minio的文件路径和名称
                        .filename("D:\\photos\\风景.png")
                        .contentType(mimeType) // 上传文件的类型
                        .build());
    }

    @Test
    void delete() throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("mediafiles")
                        .object("风景1.png")
                        .build());
    }

    @Test
    void get() throws Exception {
         GetObjectResponse mediafiles = minioClient.getObject(GetObjectArgs
                .builder()
                .bucket("mediafiles")
                .object("test/风景1.png")
                .build());


        FileOutputStream fileOutputStream = new FileOutputStream("D:\\photos\\风景1.png");
        IOUtils.copy(mediafiles,fileOutputStream);

        //md5检验完整性
        Headers headers = mediafiles.headers();
        String source_md5 = headers.get("Etag");
        source_md5 = substring(source_md5, 1, source_md5.length()-1);
        System.out.println(source_md5);
        String target_md5 = DigestUtils.md5Hex(new FileInputStream("D:\\photos\\风景1.png"));
        if (source_md5.equals(target_md5)){
            System.out.println("下载成功");
        }

    }


    @Test
    void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (int i = 0; i < 7; i++) {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("mediafiles")
                            .object("test/chunk/"+ i) // 上传到minio的文件路径和名称
                            .filename("D:\\photos\\chunk\\" + i)
                            .build());
            System.out.println("上传第"+ i +"个分块成功");
        }
    }

    @Test
    void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // List<ComposeSource> sources = new ArrayList<>();
        // for (int i = 0; i < 31; i++) {
        //     ComposeSource composeSource = ComposeSource.builder().bucket("mediafiles").object("test/chunk/"+ i).build();
        //     sources.add(composeSource);
        // }
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(7).map(i -> ComposeSource.builder().bucket("mediafiles").object("test/chunk/" + i).build()).collect(Collectors.toList());
        //指定合并后的objectname信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket("mediafiles")
                .object("test/merge.mp4")
                .sources(sources)
                .build();
        //minio默认的分块大小5m，更改不了
        minioClient.composeObject(composeObjectArgs);
    }
}
