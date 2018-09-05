package com.pinyougou.user.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制层
 * @author hongbin
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 显示用户名
     * @return
     */
    @RequestMapping("/name")
    public Map showName(){
        Map map = new HashMap();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", name);
        return map;
    }
}
