package com.demo.controllers;

import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITOptionMapper;
import com.demo.database.mapper.ITuserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/25 22:03
 */
@Controller
public class SignupController {

    @Resource
    ITuserMapper iTuserMapper;

    @Resource
    ITOptionMapper itOptionMapper;

    @GetMapping("/user/signupview")
    public String signupview(Model model){
        return "admin/user/signupview";
    }

    @PostMapping("/user/signup")
    public String signup(Model model, Tuser user){

        if(user.getIdentity() == null){
            user.setIdentity("user");
        }

        try {
            iTuserMapper.add(user);
            itOptionMapper.create_new_user(user.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "login";
    }
}
