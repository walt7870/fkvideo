package com.ssmdemo.controller;

import com.ssmdemo.forward.Constant;
import com.ssmdemo.forward.FkvideoCollection;
import com.ssmdemo.forward.FkvideoDetecor;
import com.ssmdemo.forward.MethodUtil;

import com.ssmdemo.util.RuntimeLocal;
import com.ssmdemo.ws.SystemWebSocketHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

@Controller
//@RequestMapping("fkvideo")
public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class);


    /**
     * 接收前台发起的视频检查请求
     *
     * @return
     */
    @RequestMapping(value = "/detector/v0/fkdetector", method = RequestMethod.POST)
    @ResponseBody
    public String detector(HttpServletRequest request, HttpServletResponse response) {

        logger.info("Start Fkvideo Detector");
        String sessionId = request.getSession().getId();
        FkvideoDetecor fkvideoDetecor;
        if (FkvideoCollection.getFkDetector(sessionId) != null) {
            logger.info("启动失败");
            return "errorStart";
        } else {
            fkvideoDetecor = new FkvideoDetecor();
            logger.info("新的Session：" + sessionId);
            fkvideoDetecor.setId(sessionId);
        }
        logger.info("fkvideoDetector对应的sessionId: " + sessionId);
        FkvideoCollection.setFkDetector(sessionId, fkvideoDetecor);
        try {
            fkvideoDetecor.requestFkvideo(request, response);
        } catch (Exception e) {
            logger.info("===========异常捕捉===========");
            e.printStackTrace();
            return "errorStart";
        }
        return "successStart";
    }

    /**
     * 接收sdk切出人脸后返回的请求
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/anytecfk/v0/identify", method = RequestMethod.POST)
    @ResponseBody
    public String facenapi(HttpServletRequest request, HttpServletResponse response) {
        String API = request.getRequestURI().split("fkvideo/anytecfk")[1];
        request.setAttribute("API", API);
        String reply = MethodUtil.getInstance().requestForward(request, response);
        return reply;
    }

    /**
     * 接收前台上传的视频文件
     *
     * @param request
     * @param response
     * @return
     * @throws FileNotFoundException
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        FileItemFactory factory = new DiskFileItemFactory();
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        DataOutputStream dataOutputStream = null;
        String fileName = null;
        if (isMultiPart) {
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        return "not a file";
                    } else {
                        String filedName = item.getFieldName();
                        fileName = item.getName();
//                        String filePath =request.getRealPath("video/")+fileName;
                        String filePath = Constant.VIDEO_PATH + fileName;
                        File file = new File(filePath);
                        dataOutputStream = new DataOutputStream(new FileOutputStream(file));
                        byte[] pic = item.get();
                        dataOutputStream.write(pic);
                    }
                }
            } catch (FileUploadException e) {
                response.setStatus(500);
                logger.info(e.getMessage());
            } catch (IOException e) {
                response.setStatus(500);
                logger.info(e.getMessage());
            } finally {
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return "success";
    }

    /**
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.POST)
    @ResponseBody
    public String getResultPost(HttpServletResponse response) {
        logger.info("获取视频列表访问");
        response.setCharacterEncoding("utf-8");
//		ShellConnRemote shellConnRemote = new ShellConnRemote();
//		shellConnRemote.getSession();
        String path = Constant.VIDEO_PATH;
//		String ls = shellConnRemote.exeCommand("ls "+path);
        String ls = new RuntimeLocal().execute("ls " + path);
        String[] list = ls.split("\n");
        logger.info("视频列表长度" + list.length);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            try {
                stringBuilder.append(URLEncoder.encode(list[i], "utf-8"));
            } catch (UnsupportedEncodingException e) {
                logger.info(e.getMessage());
                return "error";
            }
            if (i != list.length - 1)
                stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    /**
     * @return
     */
    @RequestMapping(value = "/stopIdentify", method = RequestMethod.GET)
    @ResponseBody
    public String sessionControl(HttpServletRequest request, HttpServletResponse response) {
        logger.info("停止视频检测");
        String sessionId = request.getSession().getId();
        String myId = request.getParameter("myId");
        FkvideoDetecor fkvideoDetecor = FkvideoCollection.getFkDetector(SystemWebSocketHandler.sidMap.get(myId + "sessionId"));
        if (fkvideoDetecor != null) {
            fkvideoDetecor.setFkstatus(FkvideoCollection.FK_STOPPING);
            while (fkvideoDetecor.getFkstatus() != FkvideoCollection.FK_CLOSEED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "stopped";
        }
        return "stop failed";
    }

}
