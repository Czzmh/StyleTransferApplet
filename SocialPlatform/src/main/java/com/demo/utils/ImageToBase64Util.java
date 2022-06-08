package com.demo.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ImageToBase64Util {
    /***  本地文件(图片、excel等)转换成Base64字符串 */
    public static String convertFileToBase64(String imgPath) {
        //读取图片字节数组
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(imgPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组进行Base64编码，得到Base64编码的字符串
        return new String(Objects.requireNonNull(Base64.encodeBase64(data)));
    }
}
