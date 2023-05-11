package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.aes.WXBizMsgCrypt;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class Controller {

    @Autowired
    private CursorMapper cursorMapper;

    @Autowired
    private ChatgptMapper chatgptMapper;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    static String token = "";
    static String encodingAesKey = "";
    static String corpid = "";
    static String sercret = "";
    static String open_kfid = "";

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping("callbackUrl")
    public  String  callbackUrl(HttpServletRequest request,@RequestBody(required = false) String postData) {
        try {
            String msg_signature = request.getParameter("msg_signature");
            String nonce = request.getParameter("nonce");
            String timestamp = request.getParameter("timestamp");
//            String echostr = request.getParameter("echostr");
//            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(token,encodingAesKey,corpid);
//            return wxBizMsgCrypt.verifyUrl(msg_signature,timestamp,nonce,echostr);
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(token,encodingAesKey,corpid);
            String sMsg =  wxBizMsgCrypt.decryptMsg(msg_signature,timestamp,nonce,postData);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(sMsg);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            org.w3c.dom.Element root = document.getDocumentElement();
            NodeList nodelist2 = root.getElementsByTagName("Token");
            String Token = nodelist2.item(0).getTextContent();

            Cursor cursor = cursorMapper.selectById(1);
            String access_token= cursor.getToken();
            JSONObject jsonMap = new JSONObject();
            jsonMap.put("cursor",cursor.getCursor());
            jsonMap.put("token",Token);
            JSONObject resp = SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/sync_msg?access_token="+access_token,jsonMap);
            if(resp.getIntValue("errcode")==42001 || resp.getIntValue("errcode")==41001){
                String resultToken = SendHttpUtils.getRequest("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+sercret);
                access_token=JSONObject.parseObject(resultToken).getString("access_token");
                cursor.setToken(access_token);
                cursorMapper.updateById(cursor);
                resp = SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/sync_msg?access_token="+access_token,jsonMap);
            }else if(resp.getIntValue("errcode")!=0){
                System.err.println(resp);
                return "200";
            }
            cursor.setCursor(resp.getString("next_cursor"));
            cursorMapper.updateById(cursor);
            JSONArray jsonArray = resp.getJSONArray("msg_list");
            for(Object obj:jsonArray){
                JSONObject o = (JSONObject)obj;
                String external_userid = o.getString("external_userid");
                if("text".equals(o.getString("msgtype"))){
                    String content = o.getJSONObject("text").getString("content");
                    System.out.println(content);
                    List<Chatgpt> list = chatgptMapper.selectList(new QueryWrapper() {{
                        eq("external_userid", external_userid);
                        like("create_time",sdf.format(new java.util.Date()));
                    }});
                    if(list.size()>9){
                        JSONObject json = new JSONObject();
                        json.put("touser",external_userid);
                        json.put("open_kfid",open_kfid);
                        json.put("msgtype","text");
                        json.put("text",new JSONObject(){{
                            put("content","抱歉，每天只回复10条");
                        }});
                        JSONObject res = SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                        if(res.getIntValue("errcode")==42001 || resp.getIntValue("errcode")==41001){
                            String resultToken = SendHttpUtils.getRequest("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+sercret);
                            access_token=JSONObject.parseObject(resultToken).getString("access_token");
                            cursor.setToken(access_token);
                            cursorMapper.updateById(cursor);
                            SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                        }else if(res.getIntValue("errcode")!=0){
                            System.err.println(res);
                            return "200";
                        }
                        return "200";
                    }
                    ChatGptRequestParameter parameter = new ChatGptRequestParameter();
                    parameter.addMessages(new ChatGptMessage("system",knowledgeMapper.selectById(1).getKnowledge()));
                    for(Chatgpt chatgpt: list){
                        parameter.addMessages(new ChatGptMessage("user",chatgpt.getContent()));
                        parameter.addMessages(new ChatGptMessage("assistant",chatgpt.getChatgpt()));
                    }
                    parameter.addMessages(new ChatGptMessage("user",content));

                    long start = System.currentTimeMillis();
                    HashMap<String,String> answer = CustomChatGpt.getAnswer(parameter);

                    long end = System.currentTimeMillis();
                    if(!answer.get("content").startsWith("ChatGPT超时")){
                        chatgptMapper.insert(new Chatgpt(){{
                            setDuration(end-start);
                            setToken(answer.get("token"));
                            setChatgpt(answer.get("content"));
                            setContent(content);
                            setExternalUserid(external_userid);
                        }});
                    }
                    JSONObject json = new JSONObject();
                    json.put("touser",external_userid);
                    json.put("open_kfid",open_kfid);
                    json.put("msgtype","text");
                    json.put("text",new JSONObject(){{
                        put("content",answer.get("content"));
                    }});
                    JSONObject res = SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                    if(res.getIntValue("errcode")==42001 || resp.getIntValue("errcode")==41001){
                        String resultToken = SendHttpUtils.getRequest("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+sercret);
                        access_token=JSONObject.parseObject(resultToken).getString("access_token");
                        cursor.setToken(access_token);
                        cursorMapper.updateById(cursor);
                        SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                    }else if(res.getIntValue("errcode")!=0){
                        System.err.println(res);
                        return "200";
                    }
                }else if(!"event".equals(o.getString("msgtype"))){
                    JSONObject json = new JSONObject();
                    json.put("touser",external_userid);
                    json.put("open_kfid",open_kfid);
                    json.put("msgtype","text");
                    json.put("text",new JSONObject(){{
                        put("content","抱歉，仅支持文本消息");
                    }});
                    JSONObject res = SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                    if(res.getIntValue("errcode")==42001 || resp.getIntValue("errcode")==41001){
                        String resultToken = SendHttpUtils.getRequest("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+sercret);
                        access_token=JSONObject.parseObject(resultToken).getString("access_token");
                        cursor.setToken(access_token);
                        cursorMapper.updateById(cursor);
                        SendHttpUtils.post("https://qyapi.weixin.qq.com/cgi-bin/kf/send_msg?access_token="+access_token,json);
                    }else if(res.getIntValue("errcode")!=0){
                        System.err.println(res);
                        return "200";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "200";
    }


    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String resultToken = SendHttpUtils.getRequest("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+sercret);
        String access_token=JSONObject.parseObject(resultToken).getString("access_token");
        String res = SendHttpUtils.postRequest("https://qyapi.weixin.qq.com/cgi-bin/kf/account/list?access_token="+access_token);
        System.out.println(res);
    }



}