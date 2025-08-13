package sample.project.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Service
public class CloudinaryService {
    private Cloudinary cloudinary;

    @Value("${cloudinary.name}")
    private String name;

    @Value("${cloudinary.key}")
    private String key;

    @Value("${cloudinary.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(
                ObjectUtils.asMap("cloud_name", name, "api_key", key, "api_secret", secret));
    }

    @SuppressWarnings("unchecked")
    public String uploadFile(MultipartFile file, boolean image) throws IOException {
<<<<<<< HEAD
        Map<String, Object> options;

        if (image) {
            options = ObjectUtils.emptyMap();
        } else {
            String publicId = "cv_" + System.currentTimeMillis(); // ensure unique
            options = ObjectUtils.asMap(
                    "resource_type", "raw",
                    "flags", "inline",
                    "format", "pdf",
                    "public_id", publicId);
        }

        System.out.println("Upload options: " + options); // debug

        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                .upload(file.getBytes(), options);

        String url = (String) uploadResult.get("secure_url");

        if (image) {
            String publicId = split(url, image);
            return publicId;
        }

        return url;
    }

    @SuppressWarnings("unchecked")
    public void deleteFile(String publicIDorUrl, boolean image) {
        String publicID = publicIDorUrl;
        Map<String, Object> options;
        if (!image) {
            publicID = split(publicIDorUrl, image);
            options = ObjectUtils.asMap("resource_type", "raw");
        } else {
            options = ObjectUtils.emptyMap();
        }
        System.out.println("public id " + publicID);
        try {

            Map result = cloudinary.uploader().destroy(publicID, options);
            System.out.println("Cloudinary destroy result: " + result);
            if (!"ok".equals(result.get("result"))) {
                System.out.println("Delete failed: " + result.get("result"));
            }

        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
        }

    }

    private String split(String url, boolean image) {
        if (url == null)
            return null;

        // Remove query parameters
        String cleanUrl = url.split("\\?")[0];

        // Match everything after /upload/ (handles /image/upload/ and /raw/upload/)
        int uploadIndex = cleanUrl.indexOf("/upload/");
        if (uploadIndex == -1)
            return null;

        String afterUpload = cleanUrl.substring(uploadIndex + "/upload/".length());

        // Remove version if present (v123456789)
        if (afterUpload.matches("^v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }

        if (image) {
            // Remove file extension
            int dotIdx = afterUpload.lastIndexOf('.');
            if (dotIdx > 0) {
                afterUpload = afterUpload.substring(0, dotIdx);
            }
        }

        return afterUpload;
    }

=======
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.emptyMap());
        String url = (String) uploadResult.get("secure_url");
        if (image) {
            String publicId = split(url);
            return publicId;

        }
        return url;

    }

    public void deleteFile(String publicID) {

        try {
            cloudinary.uploader().destroy(publicID, ObjectUtils.emptyMap());
        } catch (Exception e) {
            // Optionally log the error, but don't throw to avoid breaking user update
        }
    }

    private String split(String url) {
        // https://res.cloudinary.com/<cloud_name>/image/upload/v<version>/<public_id>.<ext>
        String[] parts = url.split("/image/upload/");
        if (parts.length < 2) {
            return null;
        }
        String afterUpload = parts[1];
        // Remove version if present
        int slashIdx = afterUpload.indexOf('/');
        if (slashIdx >= 0) {
            afterUpload = afterUpload.substring(slashIdx + 1);
        }
        // Remove extension
        int dotIdx = afterUpload.lastIndexOf('.');

        if (dotIdx > 0) {
            afterUpload = afterUpload.substring(0, dotIdx);
        }
        System.out.println("parts:" + parts + "afterupload:" + afterUpload + "dotidx" + dotIdx);
        return afterUpload;
    }
>>>>>>> 837cf3d (debugging from integration, fixed many dto errors, added url search params)
}
