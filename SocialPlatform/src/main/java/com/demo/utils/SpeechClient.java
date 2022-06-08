package com.demo.utils;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/27 16:18
 */
public class SpeechClient {
    private static volatile SpeechClient speechClient;
    private AipSpeech client = null;

    private SpeechClient(String APP_ID, String API_KEY, String SECRET_KEY){
        //client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        client = new AipSpeech("26394774", "Svrq6Avi2rtn79uN6rzEBceD", "7WuTDjaG8tfrg4OKERfDDxWaSfSet0oS");
    }

    public static SpeechClient getInstance(String APP_ID, String API_KEY, String SECRET_KEY){
        if(speechClient == null){
            synchronized (SpeechClient.class) {
                if(speechClient == null){
                    speechClient = new SpeechClient(APP_ID, API_KEY, SECRET_KEY);
                }
            }
        }
        return speechClient;
    }

    /**
     *
     * @param text 需要转成语音的文本内容
     * @param path 保存语音文件的路径
     * @return
     */
    public String generateMp3(String text, String path, HashMap<String, Object> options){
        //name用于存储生成的语音文件的文件名
        String name = null;
        //与baidu服务器通信的配置信息
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        //从baidu服务器获得合成的语音数据
        TtsResponse res = client.synthesis(text,"zh",1, options);
        byte[] data = res.getData();
        //将合成的语音数据存储为mp3格式的文件
        if(data != null){
            try{
                name = UUID.randomUUID().toString()+".mp3";
                String fileName = path + "\\" + name;
                Util.writeBytesToFileSystem(data,fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }



}
