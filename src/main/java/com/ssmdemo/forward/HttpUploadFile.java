package com.ssmdemo.forward;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 模拟表单转发http请求类
 */
public class HttpUploadFile {

    public static int status = 0;

    private final Logger logger = Logger.getLogger(HttpUploadFile.class);
    private static final String POST_URL = Constant.SDK_IP;
    private static HttpUploadFile instance;

    //单例模式
    private HttpUploadFile() {
    }

    public static HttpUploadFile getInstance() {
        if (instance == null) {
            synchronized (HttpUploadFile.class) {
                if (instance == null)
                    instance = new HttpUploadFile();
            }
        }
        return instance;
    }

    public synchronized String httpURLConnectionSDK(Map<String, String> header, Map<String, Object> param,
                                                    Map<String, Object> file, String meta) throws IOException {

        String reply;
        String BOUNDARY = "-------------------------" + System.currentTimeMillis();
        OutputStream out;
        BufferedWriter bufferedWriter;
        BufferedReader bufferedReader;

        URL url = new URL(POST_URL + header.get("API"));
        logger.info("URL :" + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(50000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod(header.get("Method"));
        connection.setRequestProperty("Authorization", "Token " + Constant.TOKEN);
        if (meta.equals("yes")) {
            logger.info("META=YES");
            connection.setRequestProperty("Content-Type", "application/json");
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            JSONObject.writeJSONString(param, bufferedWriter);
            bufferedWriter.close();
        } else if (meta.equals("isFile")) {
            logger.info("META=ISFILE");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            out = new DataOutputStream(connection.getOutputStream());
            // 文本参数
            if (null != param && param.size() != 0) {
                logger.info("******** FILE-TEXT ********");
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<Entry<String, Object>> iterator = param.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<String, Object> entry = iterator.next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue().toString();
                    logger.info(inputName + ":" + inputValue);
                    if (inputValue == null) {
                        continue;
                    }
                    stringBuilder.append("\r\n").append("--").append(BOUNDARY)
                            .append("\r\n");
                    stringBuilder.append("Content-Disposition: form-data; name=\""
                            + inputName + "\"\r\n\r\n");
                    stringBuilder.append(inputValue);
                }
                out.write(stringBuilder.toString().getBytes());
            }
            // 文件参数
            if (null != file && file.size() != 0) {
                logger.info("******** FILE-PICTURE *******");
                Iterator<Entry<String, Object>> iter = file.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, Object> entry = iter.next();
                    String filed = entry.getKey();
                    if (filed.equals("contentType"))
                        continue;
                    if (entry.getValue() == null) {
                        continue;
                    }
                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    String contentType = (String) file.get("contentType");
                    logger.info("FILE_CONTENTTYPE: " + contentType);
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("\r\n").append("--").append(BOUNDARY)
                            .append("\r\n");
                    stringBuilder.append("Content-Disposition: form-data; name=\""
                            + filed + "\"; filename=\"" + filed
                            + "\"\r\n");
                    stringBuilder.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(stringBuilder.toString().getBytes());
                    byte[] pic = (byte[]) entry.getValue();
                    out.write(pic);
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
        }
        // 读取返回数据
        StringBuilder stringBuilder = new StringBuilder();
        status = connection.getResponseCode();
        if (status == 204) {
            status = 200;
            return null;
        }
        if (200 != status) {
            logger.error("ERROR_CODE: " + status);
            bufferedReader = new BufferedReader(new InputStreamReader(
                    connection.getErrorStream()));
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
        }
        logger.info("HTTP_CODE: " + status);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reply = stringBuilder.toString();
        bufferedReader.close();
        connection.disconnect();
        logger.info(reply);
        return reply;
    }
}