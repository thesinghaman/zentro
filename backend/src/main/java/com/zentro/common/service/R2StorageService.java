package com.zentro.common.service;

import com.zentro.common.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing file storage in Cloudflare R2
 * Uses AWS S3 SDK (R2 is S3-compatible)
 */
@Slf4j
@Service
public class R2StorageService {

    @Value("${app.storage.cloudflare.r2.access-key-id}")
    private String accessKeyId;

    @Value("${app.storage.cloudflare.r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${app.storage.cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${app.storage.cloudflare.r2.public-url}")
    private String publicUrl;

    @Value("${app.storage.cloudflare.r2.endpoint}")
    private String endpoint;

    private S3Client s3Client;

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/gif");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    @PostConstruct
    public void initializeS3Client() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.US_EAST_1) // R2 doesn't use regions, but SDK requires it
                    .build();

            log.info("R2 Storage Service initialized successfully with bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("Failed to initialize R2 Storage Service", e);
            throw new RuntimeException("Failed to initialize R2 Storage Service", e);
        }
    }

    /**
     * Upload a file to R2 storage
     *
     * @param file MultipartFile to upload
     * @param folder Folder path in the bucket (e.g., "profile-pictures")
     * @return Public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateImageFile(file);

        try {
            String fileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
            String key = folder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(),
                    file.getSize()
            ));

            String fileUrl = publicUrl + "/" + key;
            log.info("File uploaded successfully to R2: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to R2", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("R2 S3 error during file upload", e);
            throw new BadRequestException("File upload failed: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Delete a file from R2 storage
     *
     * @param fileUrl Full public URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract key from full URL
            // Example: https://pub-xxx.r2.dev/profile-pictures/abc.jpg -> profile-pictures/abc.jpg
            String key = fileUrl.replace(publicUrl + "/", "");

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from R2: {}", key);

        } catch (S3Exception e) {
            log.error("Failed to delete file from R2: {}", fileUrl, e);
            // Don't throw exception on delete failure - log it and continue
        }
    }

    /**
     * Validate that the uploaded file is a valid image
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must not exceed 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only image files (JPEG, PNG, GIF) are allowed");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only jpg, jpeg, png, gif files are allowed");
        }
    }

    /**
     * Generate a unique file name using UUID
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }
}
