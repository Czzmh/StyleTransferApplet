package com.demo.controllers;

import com.demo.database.data.TComment;
import com.demo.database.data.TOption;
import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITCommentMapper;
import com.demo.database.mapper.ITOptionMapper;
import com.demo.utils.SpeechClient;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/27 17:18
 */
@Controller
public class CommentController {
    private static String APP_ID = "24589475";
    private static String API_KEY = "bDMsSh3KFR3qpXv12LSbWKbv";
    private static String SECRET_KEY = "nzV9bcdiZAha32WYwjmlS3UptOtAXuPO";

    private SpeechClient client;

    @Resource
    ITOptionMapper itOptionMapper;

    @Resource
    ITCommentMapper itCommentMapper;

    @PostMapping("/comment/sound")
    @ResponseBody
    public String sound(Model model, HttpSession session, String data){
        //从数据库中取出用户的语音配置
        TOption options = null;
        try {
            options = itOptionMapper.query_by_user_name(((Tuser)session.getAttribute("user")).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(options == null){
            options = new TOption();
        }

        String file = get_audio_file_name(data,options);
        JSONObject json = new JSONObject();
        if(null != file){
            json.put("filename",file);
        }
        return json.toString();
    }

    private String get_audio_file_name(String text, TOption options){
        SpeechClient speechCLient = SpeechClient.getInstance(APP_ID,API_KEY,SECRET_KEY);
        HashMap<String, Object> audioOption = new HashMap<>();
        audioOption.put("spd",options.getSpd());
        audioOption.put("pit",options.getPit());
        audioOption.put("per",options.getPer());
        String file = speechCLient.generateMp3(text, System.getProperty("user.dir")+"\\src\\main\\resources\\static\\audio", audioOption);
        return file;
    }

    @RequestMapping("/myComment")
    public String myComment(Model model, HttpSession session, String keyWord){
        //访问数据库查询用户的所有评论
        List<TComment> list = null;
        try {
            list = itCommentMapper.query_comment_by_user(((Tuser)session.getAttribute("user")).getName(),keyWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("list",list);
        return "comment/myCommentlist";
    }

    @PostMapping("/comment/updateView")
    public String commentUpdateView(Model model, HttpSession session, int id){
        //从数据库中找出相应的要修改的评论
        TComment currentComment = null;
        try {
            currentComment = itCommentMapper.query_comment_by_id(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("currentComment",currentComment);
        return "comment/updateCommentView";
    }

    @PostMapping("/comment/update")
    public String commentUpdate(Model model, HttpSession session, TComment cmt){
        //去数据库中修改相应的评论(注意要修改时间）
        cmt.setPubTime(new Timestamp(System.currentTimeMillis()));
        try {
            itCommentMapper.update_comment_content(cmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/myComment";
    }


    @RequestMapping("/comment/testsound")
    @ResponseBody
    public String testsound(Model model, HttpSession session, TOption options){
        String file = get_audio_file_name("这是测试音频",options);
        JSONObject json = new JSONObject();
        if(null != file){
            json.put("filename",file);
        }
        return json.toString();
    }

    @RequestMapping("/comment/delete")
    public String deleteComment(Model model, HttpSession session, String id){
        //去数据库中删除对应的评论
        try {
            itCommentMapper.delele_comment(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/myComment";
    }

}
