package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录
 * @author hongbin
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 登录后显示名字
     * @return
     */
    @RequestMapping("name")
    public Map name(){
        String name =SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName", name);

        return map;
    }
}
