package com.air.aiagent.manage;

import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author WyH524
 * @since 2025/9/27 20:47
 */
@Component
@Slf4j
public class MinioManage {

    /**
     * 从配置文件中读取
     */
    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.end-point}")
    private String endPoint;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Minio 客户端
     */
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .credentials(accessKey, secretKey)
                .endpoint(endPoint)
                .build();
    }


    /**
     * 上传生成的 PDF 文件到 MinIO 中
     * @param objectPath MinIO存储路径（如 "public/pdf/document.pdf"）
     * @param pdfFile 生成的PDF文件对象
     * @return 是否上传成功
     * @throws IllegalArgumentException 如果文件无效
     */
    public boolean uploadPDFFile(String objectPath, File pdfFile) {
        // 1. 参数校验
        if (pdfFile == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF文件对象不能为null");
        }
        if (pdfFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF文件内容为空");
        }
        try {
            // 2. 对于PDF文件，直接指定MIME类型为application/pdf
            String contentType = "application/pdf";

            // 3. 上传到MinIO
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .filename(pdfFile.getAbsolutePath())
                            .contentType(contentType)
                            .build()
            );

            log.info("文件上传成功: {} (大小: {} bytes)", objectPath, pdfFile.length());
            return true;
        } catch (Exception e) {
            log.error("文件上传失败: {} -> {}", pdfFile.getAbsolutePath(), objectPath, e);
            return false;
        }
    }

    /**
     * 获取长期有效的 PDF文件 的URL
     * @param objectPath 文件存储路径
     * @return 文件URL
     */
    public String getPDFUrl(String objectPath){
        return endPoint+"/"+bucketName+"/"+ objectPath ;
    }


    /**
     * 安全删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                boolean deleted = file.delete();
                if (deleted) {
                    log.debug("临时文件已删除: {}", file.getAbsolutePath());
                } else {
                    log.warn("临时文件删除失败: {}", file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                log.warn("没有权限删除文件: {}", file.getAbsolutePath());
            }
        }
    }


    /**
     * 获取存储桶中的文件列表
     * @param prefix 路径前缀（如 "public/"），可传null
     * @return 文件路径列表
     */
    public List<String> listFiles(String prefix) {
        List<String> files = new ArrayList<>();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println("存储桶是否存在: " + exists); // 必须输出true

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .build()
            );

            for (Result<Item> result : results) {
                files.add(result.get().objectName());
            }
        } catch (Exception e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
        }
        return files;
    }


    @PreDestroy
    public void shutdown() throws Exception {
        if (minioClient != null) {
            minioClient.close(); // 释放资源
        }
    }
}
