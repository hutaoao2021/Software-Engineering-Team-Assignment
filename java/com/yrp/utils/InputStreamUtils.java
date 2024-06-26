package com.yrp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Description:
 * @Author:Spike Wong
 * @Date:2022/7/5
 */
public class InputStreamUtils {

    //url地址中的资源（图片）转成输入流inputSatream
    public static InputStream getImageStream(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                return inputStream;
            }
        } catch (IOException e) {
            System.out.println("[Exception of getting image stream]，image url path is：" + url);
            e.printStackTrace();
        }
        return null;
    }
}
