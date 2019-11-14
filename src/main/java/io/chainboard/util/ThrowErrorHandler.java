package io.chainboard.util;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class ThrowErrorHandler implements ResponseErrorHandler {

    private int statusCode;

    private String body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        // 返回false表示不管response的status是多少都返回没有错
        // 这里可以自己定义那些status code你认为是可以抛Error
        return !response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        this.statusCode = response.getStatusCode().value();
        this.body = IOUtils.toString(response.getBody());
        throw new RuntimeException("http请求异常:" + this.body);
    }

}