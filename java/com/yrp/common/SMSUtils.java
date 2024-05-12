package com.yrp.common;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class SMSUtils {
    // 短信应用 SDK AppID
    @Value("${tx.sms.appid}")
    private   Integer appid; // SDK AppID 以1400开头
    // 短信应用 SDK AppKey
    @Value("${tx.sms.appkey}")
    private  String appkey;
    // 短信模板 ID，需要在短信应用中申请
    @Value("${tx.sms.templateId}")
    private   Integer templateId; // NOTE: 这里的模板 ID`7839`只是示例，真实的模板 ID 需要在短信控制台中申请
    // 签名
    @Value("${tx.sms.smsSign}")
    private   String smsSign; // NOTE: 签名参数使用的是`签名内容`，而不是`签名ID`。这里的签名"腾讯云"只是示例，真实的签名需要在短信控制台申请
    /**
     * 新增图书
     *
     */
    public  void sendSMS(String phone, String code ,String time) {
        try {
            ArrayList<String> params  = new ArrayList<>();
            params.add(code);
            if (time!= null && !time.equals("")){
                params.add(time);
            }
            System.out.println(phone);
            System.out.println(params);
            System.out.println("appid:"+appid);
            System.out.println("appkey:"+appkey);
            System.out.println("templateId:"+templateId);
            System.out.println("smsSign:"+smsSign);
            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
            SmsSingleSenderResult result = ssender.sendWithParam("86", phone, templateId, params, smsSign, "", "");

        } catch (HTTPException e) {
            // HTTP 响应码错误
            e.printStackTrace();
        } catch (JSONException e) {
            // JSON 解析错误
            e.printStackTrace();
        } catch (IOException e) {
            // 网络 IO 错误
            e.printStackTrace();
        }
    }
}

