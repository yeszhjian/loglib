package com.dzcx.core.log.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by chen3 on 2017/9/29.
 */

public class HttpUtils {

    public static String uploadFile(String uploadUrl, String uploadFile, String taskid) {
        try {
            File file = new File(uploadFile);
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("fileName", file.getName(), fileBody)
                    .addFormDataPart("taskId", taskid)
                    .build();
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();
            SSLUtils.SSLParams sslParams = SSLUtils.getSslSocketFactory(null, null, null);
            final OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
            OkHttpClient okHttpClient = httpBuilder
                    //设置超时
                    .connectTimeout(10 * 60, TimeUnit.SECONDS)
                    .writeTimeout(5 * 60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslParams.sslSocketFactory, sslParams.trustManager)
                    .build();
            Response execute = okHttpClient.newCall(request).execute();
            return new String(execute.body().bytes());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("LogService", "上传文件异常：" + e.getMessage() + "," + e.getClass());
        }
        return null;
    }

    /**
     * 向指定URL发送POST请求
     *
     * @param url    发送请求的URL
     * @param taskId 任务的id
     *               return String 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String taskId) {

        PrintWriter    out    = null;
        BufferedReader br     = null;
        String         result = "";
        try {
            URL realUrl = new URL(url);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
//            conn.addRequestProperty("Content-type", "application/json");
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setConnectTimeout(10 * 1000);//连接超时
            conn.setReadTimeout(15 * 1000);//读取超时
            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //发送请求参数
            out.print("taskId=" + taskId);
            //flush输出流的缓冲
            out.flush();
            //定义BufferedReader输入流来读取URL的响应
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
        return result;
    }
}
