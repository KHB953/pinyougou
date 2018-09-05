package com.hongbin.sms;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息监听类
 * @author hongbin
 */
@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;





    /**用于监听消息sms 服务器上的*/
    @JmsListener(destination = "sms")
    public void reieve(Map<String,String> map)  {
        /** 测试监听 */
        System.out.println(map.get("template_code"));
        System.out.println(map.get("sign_name"));
        System.out.println(map.get("param"));


        SendSmsResponse response = null;
        try {
            response = smsUtil.sendSms(map.get("mobile"),
                    map.get("template_code"),
                    map.get("sign_name"),
                    map.get("param"));

                    System.out.println("Code=" + response.getCode());
                    System.out.println("Message=" + response.getMessage());
                    System.out.println("RequestId=" + response.getRequestId());
                    System.out.println("BizId=" + response.getBizId());
        } catch (ClientException e) {
            e.printStackTrace();
        }

    }
}
