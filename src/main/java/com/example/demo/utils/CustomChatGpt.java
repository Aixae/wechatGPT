package com.example.demo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class CustomChatGpt {
    /**
     * 自己chatGpt的ApiKey
     */
    private static String apiKey = "";

       /**
     * 对应的请求接口
     */
    private static String url = "https://api.openai.com/v1/chat/completions";
    /**
     * 默认编码
     */
    private static Charset charset = StandardCharsets.UTF_8;


    /**
     * 相应超时时间，毫秒
     */
    private static int responseTimeout = 180000;


    public static HashMap<String,String> getAnswer(ChatGptRequestParameter chatGptRequestParameter) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建一个HttpPost
        HttpPost httpPost = new HttpPost(url);
        // 创建一个ObjectMapper，用于解析和创建json
        ObjectMapper objectMapper = new ObjectMapper();

        HttpEntity httpEntity = null;
        // 对象转换为json字符串
        httpEntity = new StringEntity(objectMapper.writeValueAsString(chatGptRequestParameter), charset);
        httpPost.setEntity(httpEntity);

        // 设置请求头
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // 设置登录凭证
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        // 用于设置超时时间
        RequestConfig config = RequestConfig
                .custom()
                .setResponseTimeout(responseTimeout, TimeUnit.MILLISECONDS)
                .build();
        httpPost.setConfig(config);
        try {
            // 提交请求
            return client.execute(httpPost, response -> {
                // 得到返回的内容
                String resStr = EntityUtils.toString(response.getEntity(), charset);
                // 转换为对象
                ChatGptResponseParameter responseParameter = null;
                try {
                    responseParameter = objectMapper.readValue(resStr, ChatGptResponseParameter.class);
                }catch (Exception e){
                    e.printStackTrace();
                    return new HashMap(){{
                        put("content","ChatGPT超时请稍后再试");
                    }};
                }
                String ans = "";
                // 遍历所有的Choices（一般都只有一个）
                for (Choices choice : responseParameter.getChoices()) {
                    ChatGptMessage message = choice.getMessage();
                    chatGptRequestParameter.addMessages(new ChatGptMessage(message.getRole(), message.getContent()));
                    String s = message.getContent().replaceAll("\n+", "\n");
                    ans += s;
                }
                // 返回信息
                String finalAns = ans;
                ChatGptResponseParameter finalResponseParameter = responseParameter;
                return new HashMap(){{
                    put("content", finalAns);
                    put("token", finalResponseParameter.getUsage().getTotal_tokens());
                }};
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return new HashMap(){{
            put("content","ChatGPT超时请稍后再试");
        }};
    }
}
