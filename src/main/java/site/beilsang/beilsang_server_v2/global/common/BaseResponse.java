package site.beilsang.beilsang_server_v2.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;

@Getter
@Setter
@JsonPropertyOrder({"statusCode", "code", "message", "data"}) // 변수 순서 지정
public class BaseResponse<T> {
    public final int statusCode; //커스텀 상태코드
    public final String code;  //Http 상태코드
    public final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public T data;

    public BaseResponse(BaseResponseCode baseResponseCode) {
        this.statusCode = baseResponseCode.getStatus().value();
        this.code = baseResponseCode.getCode();
        this.message = baseResponseCode.getMessage();
    }

    // success
    public BaseResponse(T data) {
        this.statusCode = BaseResponseCode.SUCCESS.getStatus().value();
        this.code = BaseResponseCode.SUCCESS.getCode();
        this.message = BaseResponseCode.SUCCESS.getMessage();
        this.data = data;
    }

    public BaseResponse() {
        this.statusCode = BaseResponseCode.SUCCESS.getStatus().value();
        this.code = BaseResponseCode.SUCCESS.getCode();
        this.message = BaseResponseCode.SUCCESS.getMessage();
    }

    // fail
    public BaseResponse(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    public static BaseResponse<?> error(int statusCode, String code, String message) {
        return new BaseResponse<>(statusCode, code, message);
    }
}
