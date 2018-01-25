package com.ssmdemo.forward;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

public class WsMessStore {
    private static final Logger logger = Logger.getLogger(WsMessStore.class);

    private static volatile Map<String, WebSocketSession> sessionsMap = new HashMap<>();
    private static List<String> message = new ArrayList<String>();
    private Thread pushMessageThread;

    private static WsMessStore instance;

    public static WsMessStore getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (WsMessStore.class) {
                if (instance == null)
                    instance = new WsMessStore();
            }
        }
        return instance;
    }

    public void addSession(WebSocketSession session) {

        sessionsMap.put(session.getId(), session);
    }

    public WebSocketSession getSession(String sessionId) {

        return sessionsMap.get(sessionId);
    }

    public void removeSession(WebSocketSession session) {
        logger.info("removeWebSocketSession");
        sessionsMap.remove(session.getId());
    }

    public void addMessage(String data) {

        synchronized (this) {
            message.add(data);
            logger.info("添加一条数据成功，唤醒推送线程");
            this.notifyAll();
        }

    }

    public void startPushMessThread() {
        if (null != pushMessageThread && pushMessageThread.isAlive()) {
            logger.info("推送线程活跃中。。。");
            return;
        }
        pushMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    pushMessage();
                }
            }
        });
        pushMessageThread.start();

        logger.info("推送线程启动，开始推送数据");
    }

    public void pushMessage() {

        try {
            synchronized (this) {//不关心message列表长度（有就推送，没有就等着）
                while (message.size() == 0) {
                    try {
                        logger.info("没有数据推送，等待中。。。");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String data = message.get(0);
                String msg = data.split("@@@")[1];
                String wssId = data.split("@@@")[0];
                //推送并从message列表中删除这一条数据
                logger.info("向指定用户" + wssId + "推送数据");
                if (sessionsMap.get(wssId) == null) {
                    logger.info("向用户" + wssId + "推送失败,被迫向所有用户推送该条数据");
                    Iterator<Map.Entry<String, WebSocketSession>> iterator = sessionsMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().sendMessage(new TextMessage(msg));
                    }
                    message.remove(0);
                    return;
                }
                logger.info("wss-sid" + sessionsMap.get(wssId).getAttributes().get("sid").toString());
                sessionsMap.get(wssId).sendMessage(new TextMessage(msg));
                message.remove(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endMessage(String wssId, String end) {
        if (wssId == null)
            return;
        logger.info("定向推送检测完毕断开websocket连接指令");
        try {
            if (sessionsMap.get(wssId) != null)
                sessionsMap.get(wssId).sendMessage(new TextMessage(end));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endAll() {
        logger.info("定向推送失败，被迫向所有会话推送断开指令");
        try {
            Iterator<Map.Entry<String, WebSocketSession>> iterator = sessionsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().sendMessage(new TextMessage("errorEnd"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
