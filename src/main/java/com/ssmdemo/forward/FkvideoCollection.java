package com.ssmdemo.forward;

import java.util.HashMap;
import java.util.Map;

/**
 * 建立session和fkvideoDetector的关系，管理多用户调用fkvideoDetector组件
 * 单例类
 */
public class FkvideoCollection {

    public static final int FK_RUNNING = 1;
    public static final int FK_STOPPING = 2;
    public static final int FK_CLOSEED = 0;
    public static int rtmpcam = 0;

    private static Map<String, FkvideoDetecor> fkvideoDetecors = new HashMap<>();

    private static FkvideoCollection instance;

    public static FkvideoCollection getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (FkvideoCollection.class) {
                if (instance == null)
                    instance = new FkvideoCollection();
            }
        }
        return instance;
    }

    public static FkvideoDetecor getFkDetector(String sessionId) {
        return fkvideoDetecors.get(sessionId);
    }

    public static void setFkDetector(String sessionId, FkvideoDetecor fkDetector) {
        fkvideoDetecors.put(sessionId, fkDetector);
    }

    public static void removeFkDetector(String sessionId) {
        fkvideoDetecors.remove(sessionId);
    }

    public static void setRtmpcam(int status) {
        rtmpcam = status;
    }

    public static int size() {
        return fkvideoDetecors.size();
    }
}
