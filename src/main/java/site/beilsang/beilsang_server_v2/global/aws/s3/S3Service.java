package site.beilsang.beilsang_server_v2.global.aws.s3;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.s3.path.challenge}")
    private String challengePath;

    @Value("${spring.cloud.aws.s3.path.member-profile}")
    private String memberProfilePath;

    @Value("${spring.cloud.aws.s3.path.feed}")
    private String feedPath;

    public String uploadFile(UploadPath uploadPath, MultipartFile file) {
        return uploadFile(uploadPath, null, file);
    }

    /**
     * subDirectory를 지정하여 S3에 파일을 업로드한다.
     * 예: uploadFile(MEMBER_PROFILE, "42", file) → member/profile/42/uuid_timestamp.ext
     */
    public String uploadFile(UploadPath uploadPath, String subDirectory, MultipartFile file) {

        if (file.isEmpty()) {
            log.info("Image is empty");
            return "";
        }

        String basePath = switch (uploadPath) {
            case CHALLENGE -> challengePath;
            case MEMBER_PROFILE -> memberProfilePath;
            case FEED -> feedPath;
        };

        // subDirectory가 있으면 basePath/subDirectory/ 형태로 경로 구성
        String path = (subDirectory != null && !subDirectory.isEmpty())
            ? basePath + subDirectory + "/"
            : basePath;

        // 파일 이름 설정
        String fileName = path + buildFileName(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .key(fileName)
                .build();
            RequestBody requestBody = RequestBody.fromBytes(file.getBytes());
            s3Client.putObject(putObjectRequest, requestBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build();

        return s3Client.utilities().getUrl(getUrlRequest).toString();
    }

    private String buildFileName(String originalFilename) {

        String uuid = UUID.randomUUID().toString();

        int fileExtensionIndex = originalFilename.lastIndexOf(".");
        String fileExtension = originalFilename.substring(fileExtensionIndex + 1);

        String now = String.valueOf(System.currentTimeMillis());

        return uuid + "_" + now + "." + fileExtension;
    }
}
