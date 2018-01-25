package com.ssmdemo.util;

public class FkvideoRunnable implements Runnable {

    String cmd;
    RuntimeLocal runtimeLocal;

    public FkvideoRunnable(String cmd, RuntimeLocal runtimeLocal) {
        this.cmd = cmd;
        this.runtimeLocal = runtimeLocal;
    }

    @Override
    public void run() {
        try {
            runtimeLocal.execute(cmd);
        } finally {
            runtimeLocal.closeProcess();
        }

    }
}
