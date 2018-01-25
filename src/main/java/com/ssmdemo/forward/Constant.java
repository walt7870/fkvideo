package com.ssmdemo.forward;


import com.ssmdemo.util.ConfigManager;

public class Constant {

    private static ConfigManager sdk = ConfigManager.getInstance();

    //sdk token
    public static final String TOKEN = sdk.getParameter("TOKEN");

    //http://SDK_IP:PORT
    public static final String SDK_IP = sdk.getParameter("SDK_IP");

    //video save path /home/video
    public static final String VIDEO_PATH = sdk.getParameter("VIDEO_PATH");


}
