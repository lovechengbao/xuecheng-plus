package com.xuecheng.media;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;

public class BigFileTest {


    @Test
    void testChunk() throws Exception {
        File sourceFile = new File("D:\\photos\\QQ录屏.mp4");
        //分块存储路径
        String chunkPath = "D:\\photos\\chunk\\";
        //分块大小
        int chunkSize = 1024 * 1024 * 5;
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 分块
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] chunk = new byte[chunkSize];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkPath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");

            int len = -1;
            while ((len = raf_r.read(chunk)) != -1) {
                raf_rw.write(chunk, 0, len);
                if (raf_rw.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    @Test
    void testMerge() throws Exception {

        //分块存储路径
        File chunkFolder = new File("D:\\photos\\chunk\\");
        //源文件
        File sourceFile = new File("D:\\photos\\QQ录屏.mp4");
        // 合并后文件
        File mergeFile = new File("D:\\photos\\merge\\QQ录屏.mp4");
        // 分块文件列表
        File[] files = chunkFolder.listFiles();
        //排序
        assert files != null;
        List<File> list = Arrays.asList(files);
        list.sort(Comparator.comparingInt(f -> Integer.parseInt(f.getName())));

        // 合并文件的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        byte[] chunk = new byte[1024];
        //遍历分块文件
        for (File file : list) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");

            int len = -1;
            while ((len = raf_r.read(chunk)) != -1){
                raf_rw.write(chunk, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();


        String sourceMd5 = DigestUtils.md5Hex(Files.newInputStream(sourceFile.toPath()));
        String mergeMd5 = DigestUtils.md5Hex(Files.newInputStream(mergeFile.toPath()));
        if (Objects.equals(sourceMd5, mergeMd5)){
            System.out.println("合并成功");
        }
    }
}
