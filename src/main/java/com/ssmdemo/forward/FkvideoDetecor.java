package com.ssmdemo.forward;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ssmdemo.util.RuntimeLocal;
import com.ssmdemo.ws.SystemWebSocketHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;


import com.ssmdemo.util.FkvideoRunnable;

public class FkvideoDetecor {
    private volatile int status;
    private String id;
    private int total = 0;
    private byte[] img;
    private long startTime;
    private int lockRtmpcam = 0;
    private String wssId;

    private final Logger logger = Logger.getLogger(FkvideoDetecor.class);

    private static FileItemFactory factory = new DiskFileItemFactory();
    private static Map<String, String> header = new HashMap<String, String>();
    private static Map<String, Object> param = new HashMap<String, Object>();
    private static Map<String, Object> file = new HashMap<String, Object>(2);

    public void setFkstatus(int fkstatus) {
        status = fkstatus;
    }

    public int getFkstatus() {
        return status;
    }

    public void setId(String sessionId) {
        id = sessionId;
    }

    public byte[] getImg() {
        logger.info(img.length);
        return this.img;
    }

    public void totalPlus() {
        total = total + 1;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getWssId() {
        return wssId;
    }

    public void requestFkvideo(HttpServletRequest request,
                               HttpServletResponse response) {
        logger.info("*************START***********");
        header.clear();
        param.clear();
        file.clear();
        HttpSession session = request.getSession();

        //判断是否有文件输入
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        if (isMultiPart) {
            logger.info("********* HAS FILE********");
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
        //向session中添加待比对图片
        if (file.containsKey("img")) {
            logger.info("添加待比对人脸图片");
            img = (byte[]) file.get("img");
            logger.info("图片长度： " + img.length);
            logger.info("fkDetectorId： " + id);
        }
        //获取表单文本数据
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            logger.info("*********HAS INPUT*********");
            String name = enumeration.nextElement();
            param.put(name, request.getParameter(name));
            logger.info("PARAM:" + name + "--" + "" + param.get(name));
        }
        String fileType = (String) param.get("fileType");
        if (null == fileType || !fileType.equals("file") && !fileType.equals("stream")) {
            return;
        }
        String fileName = null;
        String singlePass = null;
        if (fileType.equals("file")) {
            fileName = "file@:" + Constant.VIDEO_PATH + (String) param.get("videoName");
            singlePass = "1";
        } else if (fileType.equals("stream")) {
            fileName = (String) param.get("videoName");
            singlePass = "0";
        } else {
            return;
        }
        //===========匹配HttpServletSession和WebSocketSession=============
        String myId = "myId";
        if (param.containsKey("myId")) {
            myId = (String) param.get("myId");
        }
        if (SystemWebSocketHandler.sidMap.containsKey(myId)) {
            //每个websocket都有唯一的一个wssId关联一个fkvideoDetector
            wssId = SystemWebSocketHandler.sidMap.get(myId);
            SystemWebSocketHandler.sidMap.put(myId + "sessionId", id);
            SystemWebSocketHandler.getWebSocketSession(wssId).getAttributes().put("sessionId", id);
        }

        //===============================================================

        //配置命令参数并启动fkvideo组件
        String camid = ((Long) System.currentTimeMillis()).toString();
        session.removeAttribute("finished");
        session.setMaxInactiveInterval(300);
        String root = request.getSession().getServletContext().getRealPath("/");
        String realPath = root + "WEB-INF/classes/fkvideo.ini";
        logger.info(realPath);
        StringBuilder cmdBuilder = new StringBuilder("/usr/bin/fkvideo_detector");
        cmdBuilder.append(" -c ").append(realPath)
                .append(" --camid ").append(id)
                .append(" --api-token ").append(Constant.TOKEN)
                //.append(" --start-ts ").append("\"").append(start_ts).append("\"")//在此处加此参数程序启动不起来，但拼凑的命令无错且能执行
                .append(" --single-pass ").append(singlePass)
                .append(" --body threshold=").append((String) param.get("threshold"))
                .append(" -S ").append(fileName);

        //判断视频输出服务是否被占用
        if (FkvideoCollection.rtmpcam == 0) {
            FkvideoCollection.setRtmpcam(1);
            lockRtmpcam = 1;
            cmdBuilder.append(" --sink-url rtmp://localhost:1935/livecam");
        }
        String cmd = cmdBuilder.toString();
        cmd = cmd.replaceAll("\n", "");
        cmd = cmd.replaceAll("\r", "");
        logger.info(cmd);
        RuntimeLocal runtimeLocal = new RuntimeLocal();
        Runnable fkvideo = new FkvideoRunnable(cmd, runtimeLocal);
        Thread thread = new Thread(fkvideo);
        thread.setDaemon(true);
        startTime = new Long("1512546011710");
        thread.start();
        WsMessStore.getInstance().startPushMessThread();
        status = FkvideoCollection.FK_RUNNING;
        String end = "end";
        try {
            Thread.sleep(2000);
            logger.info("正在进行视频检测");
            while (runtimeLocal.isAlive()) {

                if (status == FkvideoCollection.FK_STOPPING) {
                    logger.info("中止视频检测");
                    end = "stop";
                    break;
                }
                Thread.sleep(1000);
            }
            runtimeLocal.closeProcess();
            status = FkvideoCollection.FK_CLOSEED;
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.error("视频分析组件启动超时");
            return;
        }

        logger.info("将该fkvideoDetector从FkvideoCollection中移除");
        FkvideoCollection.removeFkDetector(id);
        logger.info("本次视频检测共接受" + total + "个请求");
        total = 0;
        if (wssId == null) {
            logger.error("定向推送失败，wssId为null");
            // WsMessStore.getInstance().endAll();
            return;
        }
        WsMessStore.getInstance().endMessage(wssId, end);
        logger.info("释放视频输出服务占用");
        lockRtmpcam = 0;
        FkvideoCollection.setRtmpcam(0);
        //request.getSession().invalidate();
    }

}
