package com.bit.client.service;

import com.bit.utils.CommUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class Connect2Server {
    //我是个包装你的客户端, 你点击登录我就创建一个客户端  但我不想知道你
    //我想得到你的输入输出流  But 我想连接服务器(PORT, IP)  老规矩 既然是配置文件你得知道具体的Properties文件
     //emmmm... 这回你得知道IP了 因为IP在网络中 唯一标识一台服务器, 而端口号 表达你想连接那台服务器呢 兄弟

    private static final int PORT;
    private static final String IP;

    private InputStream in;
    private OutputStream out;
    private Socket socket;
    static {
        Properties properties = CommUtil.loadProperties("client.properties");
        PORT = Integer.parseInt(properties.getProperty("PORT"));
        IP = properties.getProperty("IP");
    }
    //我点击按钮  就等于说 我想在点击按钮的方法里 初始化你 那么初始化你 我得在构造方法里创建吧

    public Connect2Server() {
        try {
          socket = new Socket(IP,PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //! 来个输入输出流 ..! 您自己想像语气


    public InputStream getIn() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOut() throws IOException {
        return socket.getOutputStream();
    }

    public Socket getSocket() {
        return socket;
    }
}
