package com.demo.controllers;

import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITuserMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/28 15:22
 */
@Controller
public class adminViewController {

    @Resource
    ITuserMapper ituserMapper;

    @RequestMapping("/adminView")
    public String adminView(Model model, HttpSession session, String name){
        //判断用户是否具有访问此页面的权限
        Tuser currentUser = (Tuser)session.getAttribute("user");
        List<Tuser> list = null;
        if(null == currentUser || !("root".equals(currentUser.getIdentity()))){
            return "forbidden";
        }else {

            //从数据库中取出所有用户的信息
            try {
                 list = ituserMapper.queryAll(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            model.addAttribute("list",list);
            return "admin/administrator/userManage";
        }
    }

    @PostMapping("/admin/delete")
    public String adminDelete(Model model, HttpSession session, String name){
        String names = this.preProcess(name);
        //去数据库中删除相应的用户
        try {
            ituserMapper.deleteUser(names);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/adminView";
    }

    private String preProcess(String name){
        //对字符串进行处理
        String[] names = name.split(",");
        String par = "";
        //给每个名字加上单引号
        for(int i = 0; i< (names.length -1);i++){
            //String all = String.join("/","S","M","L");
            par += "\'" + names[i]+ "\'" + ",";
        }
        par += "\'" + names[names.length -1] + "\'";
        return par;
    }

    @PostMapping("admin/updateView")
    public String adminUpdateView(Model model,HttpSession session, String name){
        //首先在数据库中检索出该用户的信息
        Tuser user = null;
        try {
            user = ituserMapper.queryAll(name).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("userMod",user);
        //返回修改界面
        return "admin/administrator/updateView";
    }

    @PostMapping("admin/update")
    public String update(Model model, HttpSession session, Tuser user, @Param("prevName") String prevName){
        //在数据库中执行相应的修改操作
        try {
            ituserMapper.update(user,prevName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/adminView";
    }



}
