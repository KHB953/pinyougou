package com.hongbin.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送消息类 测试
 * @author hongbin
 */
@RestController
public class SendController {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination sms;

    @RequestMapping("/send")
    public String send(){
        Map map = new HashMap();
        map.put("template_code","测试SmsListener接收消息");
        map.put("sign_name","接收到了消息");
        map.put("param","ok");
        jmsTemplate.convertAndSend(sms, map);

        return "ok";
    }


}
