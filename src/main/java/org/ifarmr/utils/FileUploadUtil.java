package org.ifarmr.utils;

import lombok.experimental.UtilityClass;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.ifarmr.exceptions.FunctionErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class FileUploadUtil {

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //About 5MB

    //Acceptable image type
    public static final String IMAGE_PATTERN = "^[\\w\\-.]+\\.(?i)(jpg|png|gif|bmp|jpeg)$";


    //Replacing spaces in a file name
    public static String sanitizeFileName(final String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        // Replace spaces and underscores with hyphens
        String sanitizedFileName = fileName.replaceAll("[\\s_]+", "-");

        // For example, keeping only alphanumeric characters and hyphens
        sanitizedFileName = sanitizedFileName.replaceAll("[^a-zA-Z0-9.-]", "");

        return sanitizedFileName;
    }

    public static boolean isAllowedExtension(final String fileName, final String pattern) {
        final Matcher matcher = Pattern.compile( pattern, Pattern.CASE_INSENSITIVE).matcher(fileName);
        return matcher.matches();
    }

    public static void assertAllowed(MultipartFile file, String pattern) throws IOException {
         long fileSize = file.getSize();
        if (fileSize >= MAX_FILE_SIZE) {
            throw new FileSizeLimitExceededException("Max file size is 5MB",fileSize,MAX_FILE_SIZE);
        }

        String fileName = file.getOriginalFilename();
        fileName = sanitizeFileName(fileName);

        if (!isAllowedExtension(fileName, pattern)) {
            throw new FunctionErrorException("Only jpg, png, gif, bmp, jpeg files are supported");
        }
    }

}
