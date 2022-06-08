package com.demo.controllers;

import com.baidu.aip.util.Util;
import com.demo.database.data.TOption;
import com.demo.database.data.Tuser;
import com.demo.database.data.Tuser_face;
import com.demo.database.mapper.ITOptionMapper;
import com.demo.database.mapper.ITuserMapper;
import com.demo.servers.IUserService;
import org.apache.ibatis.annotations.Param;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/23 19:38
 */
@Controller
public class UserController {

    @Resource
    private IUserService iUserService;

    @Resource
    private ITuserMapper iTuserMapper;

    @Resource
    private ITOptionMapper itOptionMapper;

    @RequestMapping("/user/query")
    public String query(Model model, String sUserName){
        try {
            String nextPath = iUserService.query(model, sUserName);
            return nextPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping("/user/addview")
    public String addView(Model model){
        try {
            String nextPath = iUserService.addView(model);
            return nextPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping("/user/add")
    public String add(Model model, Tuser user){

        try {
            String nextPath = iUserService.add(model,user);
            return nextPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping("/user/delete")
    public String deleting(Model model, String id){
        try {
            String nextPath = iUserService.deleting(model, id);
            return nextPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping("/user/userupdateView")
    public String updateView(Model model, HttpSession session){
        return "admin/user/userUpdateView";
    }


    @RequestMapping("/user/update")
    public String update(Model model, HttpSession session, Tuser user){
        try {
            String prevName = ((Tuser)session.getAttribute("user")).getName();
            iTuserMapper.update(user, prevName);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        session.removeAttribute("user"); //使之前的用户信息失效
        session.setAttribute("user", user);

        return "redirect:/index";
    }

    @RequestMapping("/user/uploadfaceView")
    public String uploadfaceview(Model model, HttpSession session){
        return "admin/user/uploadfaceView";
    }


    @PostMapping("/user/uploadface")
    @ResponseBody
    public String uploadface(Model model, HttpSession session, @RequestParam("data") String data){

        String name = ((Tuser)session.getAttribute("user")).getName();
        try {
            iTuserMapper.updateface(data, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        json.put("nextpath","index");
        return json.toString();
    }

    @GetMapping("/user/audioSettingView")
    public String audioSettingView(Model model, HttpSession session){
        //首先查找出当前的设置并放入model中
        TOption option = null;
        try {
            option = itOptionMapper.query_by_user_name(((Tuser)session.getAttribute("user")).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("option",option);
        return "admin/user/audioSettingView";
    }

    @PostMapping("/user/audioSetting")
    public String audioSetting(Model model, HttpSession session, TOption options){
        //去数据库中更新相应的数据
        try {
            itOptionMapper.updateOption(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/index";
    }


}


