package site.beilsang.beilsang_server_v2.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private BaseResponseCode baseResponseCode;
}