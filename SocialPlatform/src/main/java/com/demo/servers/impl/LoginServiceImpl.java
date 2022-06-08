package com.demo.servers.impl;

import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITuserMapper;
import com.demo.servers.ILoginService;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/23 15:07
 */

@Service
public class LoginServiceImpl implements ILoginService {

    //注入数据访问映射接口对象
    @Resource
    private ITuserMapper iTuserMapper;

    @Override
    public String login(Model model, String userName, String password) throws Exception {
        //访问数据库(数据映射接口)查询数据
        Tuser user = iTuserMapper.queryBy(userName, password);

        if(user != null){
            model.addAttribute("user",user);
            return "redirect:/index";
        }else{
            model.addAttribute("error","用户名或密码错误");
        }
        return "login";
    }

    @Override
    public String logout(Model model, HttpSession session, SessionStatus sessionStatus) throws Exception {
        session.invalidate();
        sessionStatus.setComplete();
        return "redirect:/";
    }
}
