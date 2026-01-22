package com.nive.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author nive
 * @class RequestContextHolderUtil
 * @desc 현재 요청(Request)의 URI, Header 등 ServletContext 내에서 안전하게 추출하는 유틸리티
 *       - DispatcherServlet 컨텍스트 내에서만 사용 가능(외부 요청 시에만 쓰이며 쓰레드를 별도로 생성한 경우(@Async 등)은 적용되지 않습니다. http 요청인 경우에만 활용
 *       - Spring Web MVC 요청 흐름을 전제로 동작함
 *       - ex)관리자인지 판단할때 활용됨
 * @since 2025-05-02
 */
public class RequestContextHolderUtil {

    public static String getCurrentRequestUri() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        return request.getRequestURI();
    }

    /**
     * 관리자 요청 여부 판단 로직
     */
    private boolean isAdminRequest() {
        String path = RequestContextHolderUtil.getCurrentRequestUri();
        return path != null && path.startsWith("/api/admin");
    }
}
