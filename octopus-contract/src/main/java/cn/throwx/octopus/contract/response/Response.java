package cn.throwx.octopus.contract.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author throwable
 * @version v1
 * @description
 * @since 2020/12/25 0:13
 */
@Data // 序列化就是把对象转换成字节流，可以在通过RPC在网络上传输。
public class Response<T> implements Serializable {

    public static final Long SUCCESS = 200L;
    public static final Long ERROR = 500L;
    public static final Long BAD_REQUEST = 400L;

    private Long code;
    private String message;
    private T payload;

    public static <T> Response<T> succeed(T payload) {
        Response<T> response = new Response<>();
        response.setCode(SUCCESS);
        response.setPayload(payload);
        return response;
    }
}
