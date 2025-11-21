package com.rentkar.service;

import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for CloudinaryService
 */
class CloudinaryServicePropertyTest {
    
    private CloudinaryService getCloudinaryService() {
        // Initialize with test credentials from application.properties
        return new CloudinaryService(
            "dkoemrt4r",
            "927145473443144",
            "iZFBz9wMWACNIB1aMbiNsd1msTY"
        );
    }
    
    /**
     * Feature: item-management, Property 5: Image upload returns Cloudinary URL
     * Validates: Requirements 2.1, 2.4
     * 
     * For any valid image file (JPEG/PNG, < 5MB), uploading should return a Cloudinary URL and public ID.
     */
    @Property(tries = 100)
    void validImageUploadReturnsCloudinaryUrl(
            @ForAll("validImageFiles") MultipartFile imageFile) throws IOException {
        
        CloudinaryService cloudinaryService = getCloudinaryService();
        
        // When: uploading a valid image file
        String imageUrl = cloudinaryService.uploadImage(imageFile);
        
        // Then: should return a valid Cloudinary URL
        assertNotNull(imageUrl, "Image URL should not be null");
        assertTrue(imageUrl.startsWith("https://res.cloudinary.com/"), 
            "Image URL should be a Cloudinary URL");
        assertTrue(imageUrl.contains("dkoemrt4r"), 
            "Image URL should contain the cloud name");
    }
    
    @Provide
    Arbitrary<MultipartFile> validImageFiles() {
        return Arbitraries.of("image/jpeg", "image/png")
            .map(contentType -> {
                // Create minimal valid image files for testing
                byte[] imageData = createMinimalValidImage(contentType);
                
                return new MockMultipartFile(
                    "file",
                    "test-image." + getExtension(contentType),
                    contentType,
                    imageData
                );
            });
    }
    
    private byte[] createMinimalValidImage(String contentType) {
        if ("image/png".equals(contentType)) {
            // Minimal valid 1x1 PNG image (67 bytes)
            return new byte[] {
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
                (byte)0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
                0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte)0xB4, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42,
                0x60, (byte)0x82
            };
        } else {
            // Minimal valid 1x1 JPEG image (125 bytes)
            return new byte[] {
                (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46,
                0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01,
                0x00, 0x01, 0x00, 0x00, (byte)0xFF, (byte)0xDB, 0x00, 0x43,
                0x00, 0x03, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03,
                0x02, 0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x04,
                0x06, 0x04, 0x04, 0x04, 0x04, 0x04, 0x08, 0x06,
                0x06, 0x05, 0x06, 0x09, 0x08, 0x0A, 0x0A, 0x09,
                0x08, 0x09, 0x09, 0x0A, 0x0C, 0x0F, 0x0C, 0x0A,
                0x0B, 0x0E, 0x0B, 0x09, 0x09, 0x0D, 0x11, 0x0D,
                0x0E, 0x0F, 0x10, 0x10, 0x11, 0x10, 0x0A, 0x0C,
                0x12, 0x13, 0x12, 0x10, 0x13, 0x0F, 0x10, 0x10,
                0x10, (byte)0xFF, (byte)0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01,
                0x00, 0x01, 0x01, 0x01, 0x11, 0x00, (byte)0xFF, (byte)0xC4,
                0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x03, (byte)0xFF, (byte)0xC4, 0x00, 0x14,
                0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, (byte)0xFF, (byte)0xDA, 0x00, 0x08, 0x01, 0x01,
                0x00, 0x00, 0x3F, 0x00, 0x37, (byte)0xFF, (byte)0xD9
            };
        }
    }
    
    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
    
    /**
     * Feature: item-management, Property 6: Large files are rejected
     * Validates: Requirements 2.2
     * 
     * For any file larger than 5MB, upload should be rejected with a file size error.
     */
    @Property(tries = 100)
    void largeFilesAreRejected(@ForAll("largeFiles") MultipartFile largeFile) {
        
        CloudinaryService cloudinaryService = getCloudinaryService();
        
        // When: uploading a file larger than 5MB
        // Then: should throw IllegalArgumentException with file size error
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cloudinaryService.uploadImage(largeFile),
            "Large files should be rejected"
        );
        
        assertTrue(exception.getMessage().contains("File size exceeds maximum limit"),
            "Error message should mention file size limit");
    }
    
    @Provide
    Arbitrary<MultipartFile> largeFiles() {
        // Generate files larger than 5MB (5 * 1024 * 1024 bytes)
        return Arbitraries.integers()
            .between(5 * 1024 * 1024 + 1, 10 * 1024 * 1024) // 5MB+1 to 10MB
            .map(size -> {
                byte[] data = new byte[size];
                return new MockMultipartFile(
                    "file",
                    "large-file.jpg",
                    "image/jpeg",
                    data
                );
            });
    }
    
    /**
     * Feature: item-management, Property 7: Non-image files are rejected
     * Validates: Requirements 2.3
     * 
     * For any file that is not an image format, upload should be rejected with a file type error.
     */
    @Property(tries = 100)
    void nonImageFilesAreRejected(@ForAll("nonImageFiles") MultipartFile nonImageFile) {
        
        CloudinaryService cloudinaryService = getCloudinaryService();
        
        // When: uploading a non-image file
        // Then: should throw IllegalArgumentException with file type error
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cloudinaryService.uploadImage(nonImageFile),
            "Non-image files should be rejected"
        );
        
        assertTrue(exception.getMessage().contains("Invalid file type") || 
                   exception.getMessage().contains("Only image files are allowed"),
            "Error message should mention invalid file type");
    }
    
    @Provide
    Arbitrary<MultipartFile> nonImageFiles() {
        // Generate files with non-image content types
        return Arbitraries.of(
            "application/pdf",
            "text/plain",
            "application/json",
            "video/mp4",
            "audio/mpeg",
            "application/zip",
            "text/html",
            "application/xml"
        ).map(contentType -> {
            byte[] data = new byte[1024]; // 1KB of data
            String extension = getExtensionForContentType(contentType);
            return new MockMultipartFile(
                "file",
                "test-file." + extension,
                contentType,
                data
            );
        });
    }
    
    private String getExtensionForContentType(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> "pdf";
            case "text/plain" -> "txt";
            case "application/json" -> "json";
            case "video/mp4" -> "mp4";
            case "audio/mpeg" -> "mp3";
            case "application/zip" -> "zip";
            case "text/html" -> "html";
            case "application/xml" -> "xml";
            default -> "bin";
        };
    }
}
