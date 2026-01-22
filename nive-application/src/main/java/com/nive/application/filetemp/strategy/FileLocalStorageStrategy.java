package com.nive.application.filetemp.strategy;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.exception.BusinessRestException;
import com.nive.application.port.ApiInfoPropertiesPolicy;
import com.nive.application.port.FilePropertiesPolicy;import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author nive
 * @class LocalStorageStrategy
 * @desc 로컬 환경 파일 저장 전략 구현체
 * fileProperties.getPathPrefix() -> /uploads로 db까지 저장될 경로 명
 * @since 2025-04-24
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class FileLocalStorageStrategy implements FileStorageStrategy {

//  @Value("${file.local.path}")
//  private String localBasePath;
  private final FilePropertiesPolicy fileProperties;

  private final ApiInfoPropertiesPolicy apiInfoPropertiesPolicy;

//  @Value("${api-info.server-domain}")
//  private String serverDomain;


  @Override
  public String store(String bucketKey, MultipartFile file, String filePath, String fileName) {
    try {
      Path fullPath = Paths.get(fileProperties.getLocal().getPath(),getStoragePathPrefix(bucketKey), filePath);
      Files.createDirectories(fullPath);

      Path targetLocation = fullPath.resolve(fileName);
      file.transferTo(targetLocation);

      return "/" + filePath.replace(File.separator, "/") + "/" + fileName;    //전체 파일 경로 return
    } catch (IOException e) {
      log.error("[파일 업로드] 로컬 저장 중 에러 발생", e);
      throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).data(e).build();
    }
  }

  @Override
  public boolean exists(String bucketKey, String fullPath) {
    Path path = Paths.get(fileProperties.getLocal().getPath() + "/"+ fullPath);
    return Files.exists(path);
  }

  @Override
  public void delete(String bucketKey, String fullPath) {
    try {
      Path path = Paths.get(fileProperties.getLocal().getPath() +   "/" +fullPath);
      if(exists(bucketKey, path.toString())) {
        log.info("[파일 삭제] [삭제 파일] path : {}", path.toAbsolutePath().toString());
        Files.deleteIfExists(path);
      }else{
        log.info("[파일 삭제] [삭제 파일] 존재하지 않음 path : {}", path.toAbsolutePath().toString());
      }

    } catch (IOException e) {
      log.warn("[파일 삭제] 로컬 파일 삭제 실패: {}", fullPath);
      throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).data(e).build();
    }
  }

  @Override
  public String createThumbnail(String bucketKey, MultipartFile file, String filePath, String fileName) {
    try {
      Path fullPath = Paths.get(fileProperties.getLocal().getPath(),getStoragePathPrefix(bucketKey), filePath);
      Files.createDirectories(fullPath);

      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      BufferedImage thumbnail = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
      thumbnail.getGraphics().drawImage(originalImage, 0, 0, 300, 300, null);

      String thumbName = fileName.replace(".", "_thumb.");
      Path targetLocation = fullPath.resolve(thumbName);
      ImageIO.write(thumbnail, "jpg", targetLocation.toFile());

      return "/" + filePath.replace(File.separator, "/") + "/" + thumbName;
    } catch (IOException e) {
      log.error("[썸네일 생성] 실패: {}", fileName, e);
      throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).data(e).build();
    }
  }



  @Override
  public String getAccessUrl(String bucketKey, String filePath, String fileUploadName) {
    String serverDomain = apiInfoPropertiesPolicy.getServerDomain();
    if(!StringUtils.hasText(serverDomain)) {
      log.warn("[로컬] [파일경로 처리] [도메인 없음] [하드코딩 처리] - {}", "core.nive.com");
      serverDomain = "core.nive.com";
    }
    String scheme = serverDomain.contains("localhost") || serverDomain.contains("127.0.0.1") ? "http" : "https";

    // serverDomain 뒤에 "/"가 없으면 추가
    String domain = serverDomain.endsWith("/") ? serverDomain : serverDomain + "/";

    //[NOTE] 시작 폴더 명에 따라 kyc 등은 임시 경로를 처리해야될 수 있으므로 주의(개인정보파일이 대부분이므로 정적 파일 경로를 바로 보여주면 안된다)

    // filePath 앞뒤 슬래시 정리
    String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
    normalizedPath = normalizedPath.endsWith("/") ? normalizedPath : normalizedPath + "/";

    return scheme + "://" + domain + normalizedPath + fileUploadName;
  }

  @Override
  public String getRepositoryCode() {
    return "LOCAL";
  }

  @Override
  public String getStoragePathPrefix(String bucketKey) {
    return fileProperties.getLocal().getPathPrefix();
  }

  @Override
  public Resource getResource(String bucketKey, String filePath, String fileUploadName) {
    File file = new File(getAccessUrl(bucketKey, filePath, fileUploadName));
    if (!file.exists()) {
      log.info("[로컬] [파일 resource 없음] bucketKey : {}, filePath : {}, fileUploadName : {}", bucketKey, filePath, fileUploadName);
      throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
    }
    return new FileSystemResource(file);
  }
}
