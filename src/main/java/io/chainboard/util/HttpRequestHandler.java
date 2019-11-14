package io.chainboard.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpRequestHandler {

    public static String getDefault(String url) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Accept", "application/json");
        return getWithHeader(url, requestHeaders);
    }

    public static String getWithOutHeader(String url) {
        return getWithHeader(url, null);
    }

    public static String getWithHeader(String url, HttpHeaders requestHeaders) {
        HttpEntity<String> requestEntity = null;
        if (requestHeaders != null)
            requestEntity = new HttpEntity<>(requestHeaders);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);// 设置超时
        requestFactory.setReadTimeout(3000);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        ThrowErrorHandler throwErrorHandler = new ThrowErrorHandler();
        restTemplate.setErrorHandler(throwErrorHandler);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return responseEntity.getBody();
    }

    public static String postWithHeader(String url, JSONObject data, HttpHeaders requestHeaders) {
        HttpEntity<String> requestEntity = null;
        if (data == null)
            requestEntity = new HttpEntity<>(requestHeaders);
        else
            requestEntity = new HttpEntity<>(data.toJSONString(), requestHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ThrowErrorHandler throwErrorHandler = new ThrowErrorHandler();
        restTemplate.setErrorHandler(throwErrorHandler);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return responseEntity.getBody();
    }

    public static String formPost(String url, MultiValueMap<String, String> params){
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        //MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
//  也支持中文
//        params.add("username", "用户名");
//        params.add("password", "123456");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
//  执行HTTP请求
        ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }
}
