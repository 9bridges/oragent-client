package net.tbsoft.fzsclient.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Response<T> {

    /**
     * 响应代码或http status
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String msg;
    /**
     * 响应数据或http 响应body content(String)
     */
    private T data;

    public static <T> Response<T> success(int code, String msg, T data) {
        return new Response<>(code, msg, data);
    }

    public static <T> Response<T> failure(int code, String msg) {
        return new Response<>(code, msg, null);
    }

    public static <T> Response<T> success(int code, String msg) {
        return success(code, msg, null);
    }


}