package com.ssmdemo.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RuntimeLocal {
    private static Logger logger = Logger.getLogger(RuntimeLocal.class);
    private static final Runtime runtime = Runtime.getRuntime();
    public Process process = null;

    public String execute(String cmd) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            logger.info("本地进程执行中。。。");
            process = runtime.exec(cmd);
            br = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), "utf-8"));
            String tmp = null;
            while ((tmp = br.readLine()) != null) {
                stringBuilder.append(tmp).append("\n");
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return stringBuilder.toString();
    }

    public void closeProcess() {
        while (process.isAlive()) {
            process.destroy();
        }
    }

    public boolean isAlive() {
        return process.isAlive();
    }
}
