package com.ssmdemo.ws;

import com.ssmdemo.forward.FkvideoCollection;
import com.ssmdemo.forward.FkvideoDetecor;
import com.ssmdemo.forward.WsMessStore;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.ServerSocket;
import java.util.*;

@Component
public class SystemWebSocketHandler implements WebSocketHandler {
    private static final Map<String,WebSocketSession> sessions = new HashMap<String,WebSocketSession>();
    private static final Logger logger = Logger.getLogger(SystemWebSocketHandler.class);
    public static volatile Map<String,String> sidMap = new HashMap<>();

    public static WebSocketSession getWebSocketSession(String wssId){
        return sessions.get(wssId);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try{
            logger.info("用户："+session.getId()+"连接Server成功");
            sessions.put(session.getId(),session);
            WsMessStore.getInstance().addSession(session);
        }catch (Exception e){
            logger.error(e.getMessage());
        }

    }

    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> wsm) throws Exception {
        //每个websocket连接成功的用户都会传递一个唯一标识sid用来与httpServletSession关联
        String sid = wsm.getPayload().toString();
        wss.getAttributes().put("sid",sid);
        sidMap.put(sid,wss.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession wss, Throwable thrwbl) throws Exception {
        if(wss.isOpen()){
            wss.close();
        }
        sessions.remove(wss.getId());
        WsMessStore.getInstance().removeSession(wss);
//        System.out.println("WebSocket出错！");
        logger.info("ERROR! 用户："+wss.getId()+"从Server断开");
        String sid = (String) wss.getAttributes().get("sid");
        SystemWebSocketHandler.sidMap.remove(sid);
        SystemWebSocketHandler.sidMap.remove(sid+"sessionId");
        Object sessionId = wss.getAttributes().get("sessionId");
        if(sessionId!=null){
            FkvideoDetecor fkvideoDetecor = FkvideoCollection.getFkDetector((String) sessionId);
            if(fkvideoDetecor!=null)
                fkvideoDetecor.setFkstatus(FkvideoCollection.FK_STOPPING);
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus cs) throws Exception {
        sessions.remove(wss.getId());
        WsMessStore.getInstance().removeSession(wss);
        logger.info("用户："+wss.getId()+"从Server断开");
        String sid = (String) wss.getAttributes().get("sid");
        SystemWebSocketHandler.sidMap.remove(sid);
        SystemWebSocketHandler.sidMap.remove(sid+"sessionId");
        Object sessionId = wss.getAttributes().get("sessionId");
        if(sessionId!=null){
            FkvideoDetecor fkvideoDetecor = FkvideoCollection.getFkDetector((String) sessionId);
            if(fkvideoDetecor!=null)
                fkvideoDetecor.setFkstatus(FkvideoCollection.FK_STOPPING);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}