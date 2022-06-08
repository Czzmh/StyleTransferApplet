package com.demo.utils;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/25 14:29
 */
public class FaceClient {
    private static volatile FaceClient faceClient = null;
    private AipFace client = null;
    private FaceClient(String APP_ID, String API_KRY, String SECRET_KEY){
        client = new AipFace(APP_ID,API_KRY,SECRET_KEY);
    }
    public static FaceClient getInstance(String APP_ID, String API_KEY, String SECRET_KEY){
        if(faceClient == null){
            synchronized (FaceClient.class){
                if(faceClient == null){
                    faceClient = new FaceClient(APP_ID, API_KEY, SECRET_KEY);
                }
            }
        }
        return faceClient;
    }

    /**
     * faceCompare函数，用于比较两个人像是否为同一个人
     * @param face1 base64编码格式的图片
     * @param face2 base64编码格式的图片
     * @return 两个人脸是否配对
     */
    public boolean faceCompare(String face1, String face2){
        //MatchRequest是com.baidu.aip.face提供的封装人像图片用于人像比对请求的类
        MatchRequest req1 = new MatchRequest(face1, "BASE64");
        MatchRequest req2 = new MatchRequest(face2, "BASE64");
        ArrayList<MatchRequest> requests = new ArrayList<MatchRequest>();
        requests.add(req1);
        requests.add(req2);
        //client是此类的私有成员 private AipFace client = null;
        //AipFace由com.baidu.aip.face提供，用于人脸特征识别、人脸比对等功能
        //通过match()函数比对两个人像，返回JSON格式的数据
        JSONObject res = client.match(requests);
        Object object = res.get("result");
        if (object == null || object.toString().equals("null")) {
            return false;
        } else {
            res = (JSONObject) object;
            double result = res.getDouble("score"); // score里存放了比对结果
            if (result >= 95) { //相似度>95%则认为是同一个人。
                return true;
            } else {
                return false;
            }
        }
    }
}
