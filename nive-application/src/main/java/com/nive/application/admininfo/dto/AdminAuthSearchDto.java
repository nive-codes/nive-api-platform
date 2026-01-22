package com.nive.application.admininfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author nive
 * @class AdminAuthSearchDto
 * @desc 관리자 검색용 dto
 * @since 2025-06-09
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 검색용 dto")
public class AdminAuthSearchDto {

      @Schema(description = "검색어 - 이름, 성, 로그인id, email")
      private String searchKeyword;

      @Schema(description = "페이지 번호-페이징이 있는 경우에만 적용됩니다.", example = "0")
      @Min(0)
      @Builder.Default
      private int page = 0;      // 페이지 번호 (0부터 시작)

      @Schema(description = "페이지 크기-페이징이 있는 경우에만 적용됩니다.", example = "10")
      @Min(1)
      @Builder.Default
      private int size = 10;      // 페이지 크기


      public Pageable toPageable() {
          return PageRequest.of(page, size);
      }
}
