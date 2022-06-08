package com.demo.controllers;

import com.demo.database.data.TComment;
import com.demo.database.data.TPassage;
import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITCommentMapper;
import com.demo.database.mapper.ITPassageMapper;
import com.demo.utils.ImageToBase64Util;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/26 20:49
 */
@Controller
public class PsgController {

    @Resource
    ITPassageMapper itPassageMapper;

    @Resource
    ITCommentMapper itCommentMapper;

    @RequestMapping("/psg/all")
    public String psglist(Model model, HttpSession session, String keyWord){ //通过keyWord对文章进行筛选
        List<TPassage> list = null;
        //通过关键字keyWord在数据库中检索文章，将结果存储在list中，
        try {
            list = itPassageMapper.query_all(keyWord, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //往model中添加数据"passageList"：list；
        model.addAttribute("passageList",list);
        return "psg/passagelist";
    }

    @RequestMapping("/psg/mypsg")
    public String mypsglist(Model model, HttpSession session, String keyWord){ //通过keyWord对文章进行筛选
        List<TPassage> list = null;
        try {
            list = itPassageMapper.query_all(keyWord, ((Tuser)session.getAttribute("user")).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("passageList",list);
        return "psg/myPassagelist";
    }

    @GetMapping("/psg/addpsgView")
    public String addpsgView(Model model, HttpSession session){
        return "psg/addpsgView";
    }

    @GetMapping("/psg/typemove")
    public String typemove(Model model, HttpSession session){
        return "psg/typemove";
    }

    @PostMapping("/psg/addpsg")
    public String addpsg(Model model, HttpSession session, String title, String content, String tag, MultipartFile file){
        String userName = ((Tuser)session.getAttribute("user")).getName();
        TPassage passage = new TPassage();
        passage.setAuthor(userName);
        passage.setTitle(title);
        passage.setContent(content);
        passage.setTag(tag);
        passage.setPubTime(new Timestamp(System.currentTimeMillis()));
        String basePath = "";
        try {
            basePath = ResourceUtils.getURL("classpath:").getPath() + "static/upload/";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(basePath);
        String OriginalFilename = file.getOriginalFilename();//获取原文件名
        String suffixName = OriginalFilename.substring(OriginalFilename.lastIndexOf("."));//获取文件后缀名
        //重新随机生成名字
        String filename = UUID.randomUUID().toString() +suffixName;
        File localFile = new File(basePath+"\\"+filename);
        try {
            file.transferTo(localFile); //把上传的文件保存至本地
            /**
             * 这里应该把filename保存到数据库,供前端访问时使用
             */
        }catch (IOException e){
            e.printStackTrace();
        }
        passage.setImgurl("upload/"+filename);
        try {
            itPassageMapper.addPsg(passage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/psg/all";
    }

    @GetMapping("/psg/detal/{id}")
    public String passageDetail(Model model, HttpSession session,@PathVariable("id") int passageId){

        TPassage passage = null;
        List<TComment> comments = null;
        //去数据库中查询id对应的文章
        try {
            passage = itPassageMapper.query_by_id(passageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("currentPassage",passage);
        try {
            comments = itPassageMapper.query_comment_by_passage_id(passageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("comments", comments);
        //去数据库中查询id对应的文章的所有评论
        return "psg/passageDetail";
    }

    @GetMapping("/psg/addCommentView/{id}")
    public String addCommentView(Model model, HttpSession session,@PathVariable("id") String psgId){
        model.addAttribute("psgId",psgId);
        return "psg/addCommentView";
    }

    @PostMapping("/psg/addComment")
    public String addComment(Model model, HttpSession session,int psgId, String comment){
        //获取当前的用户
        Tuser user = (Tuser)session.getAttribute("user");
        String userName = user.getName();

        //封装进TComment对象
        TComment cmt = new TComment();
        cmt.setPassageId(psgId);
        cmt.setUserName(userName);
        cmt.setComment(comment);
        cmt.setPubTime(new Timestamp(System.currentTimeMillis()));
        try {
            itCommentMapper.insert_comment(cmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/psg/detal/"+psgId;
    }

    @PostMapping("psg/delete")
    public String deletepsg(Model model, HttpSession session, String id){
        //从数据库中删除相应的文章
        try {
            itPassageMapper.delPsg(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/psg/mypsg";
    }

    @PostMapping("psg/updateView")
    public String updatepsgView(Model model, HttpSession session,int id){
        //首先在数据库中查询出要修改的文章
        TPassage psg = null;
        try {
            psg = itPassageMapper.query_by_id(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("currentPsg",psg);
        return "psg/updatePsgView";
    }

    @PostMapping("psg/update")
    public String updatepsg(Model model, HttpSession session, TPassage psg, MultipartFile file){
        //在数据库中执行相应的更行操作
        if(file.getOriginalFilename().length() > 0){
            String basePath = "";
            try {
                basePath = ResourceUtils.getURL("classpath:").getPath() + "static/upload/";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(basePath);
            String OriginalFilename = file.getOriginalFilename();//获取原文件名
            String suffixName = OriginalFilename.substring(OriginalFilename.lastIndexOf("."));//获取文件后缀名
            //重新随机生成名字
            String filename = UUID.randomUUID().toString() +suffixName;
            File localFile = new File(basePath+"\\"+filename);
            try {
                file.transferTo(localFile); //把上传的文件保存至本地
                /**
                 * 这里应该把filename保存到数据库,供前端访问时使用
                 */
            }catch (IOException e){
                e.printStackTrace();
            }
            psg.setImgurl("upload/"+filename);
        }
        psg.setPubTime(new Timestamp(System.currentTimeMillis()));
        try {
            itPassageMapper.updatepsg(psg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/psg/mypsg";
    }

    @GetMapping(value = "/psg/img",produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public String getImage() throws Exception {
        String basePath = "C:\\module\\images\\input\\B";
        File baseFile = new File(basePath);
        File inputFile = baseFile.listFiles()[0];
        String base64= ImageToBase64Util.convertFileToBase64(inputFile.getAbsolutePath());
        return base64;
    }

    @PostMapping("/psg/typeUpload")
    @ResponseBody
    public boolean typeUpload(HttpSession session, MultipartFile file){
        String basePath = "C:\\module\\images\\input\\B";
        File baseFile = new File(basePath);
        for(File imgfile:baseFile.listFiles()){
            boolean flag = imgfile.delete();
        }
        String OriginalFilename = file.getOriginalFilename();//获取原文件名
        String suffixName = OriginalFilename.substring(OriginalFilename.lastIndexOf("."));//获取文件后缀名
        //重新随机生成名字
        String filename = UUID.randomUUID().toString() +suffixName;
        File localFile = new File(basePath+"\\"+filename);
        try {
            file.transferTo(localFile); //把上传的文件保存至本地
        }catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }

    @GetMapping(value = "/psg/imgView",produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public String imgView() throws Exception {
        executeExe();
        String basePath = "C:\\module\\images\\output\\B2A";
        File baseFile = new File(basePath);
        File inputFile = baseFile.listFiles()[0];
        String base64= ImageToBase64Util.convertFileToBase64(inputFile.getAbsolutePath());
        return base64;
    }

    @RequestMapping(value = { "/psg/downloadImg" }, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity download() throws IOException {
        String basePath = "C:\\module\\images\\output\\B2A";
        File baseFile = new File(basePath);
        File inputFile = baseFile.listFiles()[0];
        String fileName = UUID.randomUUID().toString()+".png";
        @SuppressWarnings("resource")
        InputStream in = new FileInputStream(inputFile);
        byte[] body = null;
        body = new byte[in.available()];
        in.read(body);// 读入到输入流里面
        HttpHeaders headers = new HttpHeaders();// 设置响应头
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        HttpStatus statusCode = HttpStatus.OK;// 设置响应吗
        ResponseEntity response = new ResponseEntity(body, headers, statusCode);
        return response;
    }

    // An highlighted block
    public void executeExe() {
        try {
            Runtime run = Runtime.getRuntime();
            Process pro =run.exec("python C:\\module\\RMIGAN\\test.py");
            InputStream ers= pro.getErrorStream();
            pro.waitFor();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
