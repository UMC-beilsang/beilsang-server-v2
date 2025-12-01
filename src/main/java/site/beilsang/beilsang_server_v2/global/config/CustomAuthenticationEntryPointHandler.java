package site.beilsang.beilsang_server_v2.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    // 인증되지 않은 사용자가 접근 시 동작

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        String exception = (String) request.getAttribute("exception");
        BaseResponseCode errorCode;

        // ① 예외 분기
        if ("NO_JWT".equals(exception)) {
            errorCode = BaseResponseCode.NOT_FOUND_JWT;
        }
        else if ("EXPIRED_JWT".equals(exception)) {
            errorCode = BaseResponseCode.EXPIRED_JWT;
        }
        else if ("INVALID_JWT".equals(exception)) {
            errorCode = BaseResponseCode.INVALID_JWT;
        }
        else {
            errorCode = BaseResponseCode.BAD_REQUEST;
        }

        // ② 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        // ③ BaseResponse 생성
        BaseResponse<Object> errorResponse = new BaseResponse<>(errorCode);

        // ④ JSON 변환 후 응답 작성
        String jsonBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonBody);
    }
}
