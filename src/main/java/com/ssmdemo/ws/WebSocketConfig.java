package com.ssmdemo.ws;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements
        WebSocketConfigurer {
    private static final Logger logger = Logger.getLogger(SystemWebSocketHandler.class);

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 用来注册websocket server实现类，第二个参数是访问websocket的地址
        registry.addHandler(systemWebSocketHandler(), "/fkFacePushWs");
        // 使用Sockjs的注册方法
        registry.addHandler(systemWebSocketHandler(), "/sockjs/fkFacePushWs").withSockJS();

    }

    public WebSocketHandler systemWebSocketHandler() {
        return new SystemWebSocketHandler();
    }

}