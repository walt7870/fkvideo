package com.ssmdemo.forward;

import com.ssmdemo.util.Base64Encrypt;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 接收用户请求并解析
 */
public class MethodUtil {
    private static MethodUtil instance;
    private final Logger logger = Logger.getLogger(MethodUtil.class);

    private static FileItemFactory factory = new DiskFileItemFactory();
    private static Map<String, String> header = new HashMap<String, String>();
    private static Map<String, Object> param = new HashMap<String, Object>();
    private static Map<String, Object> file = new HashMap<String, Object>(2);
    private static JSONParser jsonParser = new JSONParser();
    private static String meta;

    private MethodUtil() {
    }

    public static MethodUtil getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (MethodUtil.class) {
                if (instance == null)
                    instance = new MethodUtil();
            }
        }
        return instance;
    }

    public String requestForward(HttpServletRequest request,
                                 HttpServletResponse response) {
        logger.info("*************START***********");
        String SDKreply = null;
        String API = (String) request.getAttribute("API");
        header.clear();
        param.clear();
        file.clear();
        meta = "no";
        String sessionId = "";

        //判断是否有文件输入
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        if (isMultiPart) {
            logger.info("********* HAS FILE********");
            meta = "isFile";

            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        //文本
                        String filedName = item.getFieldName();
                        String value = item.getString("utf-8");
                        param.put(filedName, value);
                        logger.info("文本域：" + filedName + "——" + value);
                        if (filedName.equals("timestamp"))
                            logger.info(new Date());
                        //有上传文件
                    } else {
                        file.put("contentType", item.getContentType());
                        logger.info("contentType:" + item.getContentType());
                        String filedName = item.getFieldName();
                        String fileName = item.getName();
                        logger.info("文件控件: " + filedName + "--" + fileName);
                        byte[] pic = item.get();
                        logger.info("PICTURE.LENGTH:" + pic.length);
                        file.put(filedName, pic);
                    }
                }
            } catch (FileUploadException e) {
                response.setStatus(500);
                logger.error("*****FILE_UPLOAD_FAIL*****");
                logger.error(e.getMessage());
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //获取表单文本数据
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            logger.info("*********HAS INPUT*********");
            meta = "yes";
            String name = enumeration.nextElement();
            if ((name.equals("max_id") || name.equals("min_id")) && request.getMethod().equals("GET")) {
                meta = "no";
            } else {
                param.put(name, request.getParameter(name));
            }
            logger.info("PARAM:" + name + "--" + "" + param.get(name));
        }
        //转发和设定报头信息
        header.put("Method", request.getMethod());
        header.put("API", API);
        logger.info(request.getAttribute("API").toString());
        //正常的anytec请求正常返回
        if (!param.containsKey("cam_id")) {
            logger.info("无fkvideo_detector的正常API");
            String anytecReply = "ERROR_SDK_REPLY";
            try {
                anytecReply = HttpUploadFile.getInstance().httpURLConnectionSDK(header, param, file, meta);
            } catch (IOException e) {
                logger.error("exception:" + e.getMessage());
                return null;
            }
            return anytecReply;
        }
        //调整参数
        if (!param.containsKey("cam_id")) {
            logger.error("cam_id为空");
            return null;
        }
        sessionId = (String) param.get("cam_id");
        FkvideoDetecor fkvideoDetecor = FkvideoCollection.getFkDetector(sessionId);
        long startTime = fkvideoDetecor.getStartTime();
        String wssId = fkvideoDetecor.getWssId();
        if (fkvideoDetecor.getImg() == null) {
            logger.error("fkvideoDetector待比对图片为空");
            return null;
        }
        byte[] img = fkvideoDetecor.getImg();
        header.put("API", "/v0/verify");
        String face0 = null;
        String view = null;
        file.put("photo1", img);
        file.put("photo2", file.get("photo"));
        if (file.containsKey("face0")) {
            //face0(人脸切图)或photo(全图)
            face0 = "data:image;base64," + Base64Encrypt.byteArrayToString((byte[]) file.get("face0"));
            view = "data:image;base64," + Base64Encrypt.byteArrayToString((byte[]) file.get("photo"));
        }
        //face0(人脸切图)或photo(全图)

        Map<String, Object> arg = new HashMap<String, Object>();
        if (param.containsKey("threshold"))
            arg.put("threshold", param.get("threshold"));

        if (param.containsKey("bbox")) {
            arg.put("bbox2", param.get("bbox"));
        }
        if (param.containsKey("threshold"))
            arg.put("threshold", param.get("threshold"));
        String times = (String) param.get("timestamp");
        LocalDateTime localDateTime = LocalDateTime.parse(times);
        // localDateTime = localDateTime.plusHours(Long.parseLong(Constant.TIME_PLUS_HOURS));
        int year = localDateTime.getYear();
        int mouth = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        int minute = localDateTime.getMinute();
        int second = localDateTime.getSecond();
        long timestamp = LocalDateTime.of(year, mouth, day, hour, minute, second).atZone(ZoneId.systemDefault()).
                toInstant().toEpochMilli();
        //调用FindFaceSDK
        try {
            SDKreply = HttpUploadFile.getInstance().httpURLConnectionSDK(header, arg, file, meta);
            if (HttpUploadFile.status != 200) {
                logger.info("服务器responseCode!=200");
                return null;
            }
            response.setStatus(HttpUploadFile.status);
        } catch (IOException e) {
            response.setStatus(400);
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        response.setHeader("Cache-control", "no-cache");
        //统计请求数量==========================================
        fkvideoDetecor.totalPlus();
        //====================================================
        //过滤threshold低于传入阈值的verify响应
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(SDKreply);
            boolean verified = (boolean) jsonObject.get("verified");
            if (verified == false) {
                logger.info("人脸匹配度不达标");
                return null;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        logger.info("fkvideo响应数据处理");
        //包装响应数据，添加fkvideo_detector截取人脸的时间戳和人脸缩略图
        String add = "\"timestamp\":\"" + timestamp + "\",\"catchFace\":\"" + face0 + "\",\"catchView\":\"" + view + "\",\"results\"";
        String reply = SDKreply.replaceFirst("\"results\"", add);
        logger.info("添加匹配人脸");
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"begin\":\"" + startTime + "\",\"faces\":[");
        stringBuilder.append(reply);
        stringBuilder.append("]}");
        String message = wssId + "@@@" + stringBuilder.toString();
        WsMessStore.getInstance().addMessage(message);
        return null;
    }

}
