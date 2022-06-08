package com.demo.controllers;

import com.demo.database.data.Tuser;
import com.demo.database.data.Tuser_face;
import com.demo.database.mapper.ITuserMapper;
import com.demo.servers.ILoginService;
import com.demo.utils.FaceClient;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 登录及注销的控制器
 * @author Shen RuiJin
 * @createTime 2021/7/23 14:52
 */
@Controller
@SessionAttributes({"user"})
public class LoginController {

    private static String APP_ID = "24589475";
    private static String API_KEY = "bDMsSh3KFR3qpXv12LSbWKbv";
    private static String SECRET_KEY = "nzV9bcdiZAha32WYwjmlS3UptOtAXuPO";

    @Resource
    ILoginService iLoginService;

    @Resource
    ITuserMapper iTuserMapper;

    @PostMapping("/login")
    public String login(Model model, String userName, String password){
        try{
            String nextPath = iLoginService.login(model, userName, password);
            return nextPath;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "/";
    }

    @GetMapping("/logout")
    public String logout(Model model, HttpSession session, SessionStatus status){

        try {
            String nextPath = iLoginService.logout(model,session,status);
            return nextPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/";
    }

    @PostMapping("/facelogin")
    @ResponseBody
    public String facelogin(Model model, HttpSession session, @RequestParam("data") String data){

        FaceClient faceClient = FaceClient.getInstance(APP_ID, API_KEY, SECRET_KEY);
        String username = data.split("\\%")[0];
        String face1 = data.split("\\%")[1];

        String nextPath = "null";
        JSONObject json = new JSONObject();

        Tuser_face outcome = null;
        List<Tuser> tuser = null;
        //从数据库中取出名字为username的图片
        try {
            outcome = iTuserMapper.queryFace(username);
            tuser = iTuserMapper.queryAll(username);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(outcome == null){
            nextPath = ""; //登录失败
            model.addAttribute("error","用户名错误或人脸识别失败");

        }else{
            String face2 = outcome.getFace();
            boolean isMatch = faceClient.faceCompare(face1,face2);
            if(isMatch == true){
               nextPath =  "index";
                session.setAttribute("user",tuser.get(0));
            }else{
                nextPath = "";
                model.addAttribute("error","用户名错误或人脸识别失败");
            }
        }
        json.put("nextpath",nextPath);
        return json.toString();
    }


}
