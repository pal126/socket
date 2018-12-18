package com.pal.socket;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class Client {
    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {
        Socket socket = createSocket();

        initSocket(socket);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 3000);

        todo(socket);

        socket.close();
    }

    private static void todo(Socket socket) {

    }

    private static void initSocket(Socket socket) {

    }

    private static Socket createSocket() throws IOException {
        /*// 无代理模式 等效于空构造函数
        Socket socket = new Socket(Proxy.NO_PROXY);

        // 新建一份具有http代理的套接字 传输数据将通过www.baidu.com 8080端口转发
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8080));

        // 新建一个套接字 并且直接连接到本地20000的服务器上
        socket = new Socket("localhost", PORT);
        socket = new Socket(Inet4Address.getLocalHost(), PORT);

        // 新建一个套接字 并且直接连接到本地20000的服务器上 并且绑定到本地20001端口上
        socket = new Socket("localhost", PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
        socket = new Socket(Inet4Address.getLocalHost(), PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
*/
        Socket socket = new Socket();
        // 绑定到本地20001端口
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));

        return socket;
    }
}
