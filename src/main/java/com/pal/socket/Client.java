package com.pal.socket;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;

public class Client {

    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {
        // 创建一个socket
        Socket socket = createSocket();

        // init socket
        initSocket(socket);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 3000);

        todo(socket);

        socket.close();
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

    private static void initSocket(Socket socket) throws SocketException {
        // 超时时间3s
        socket.setSoTimeout(3000);

        // 是否复用未完全关闭的socket地址 对于指定bind操作之后的socket有效
        socket.setReuseAddress(true);

        // 是否开启Nagle算法
        socket.setTcpNoDelay(false);

        // 是否需要再长时间无数据响应时发送确认数据(类似心跳包) 时间大约2小时
        socket.setKeepAlive(true);

        /*
         * 对于close关闭操作行为进行怎样的处理 默认为false 0
         * false 0: 默认情况 关闭时立即返回 底层系统接管输出流 将缓冲区内的数据发送完成
         * true 0: 关闭时立即返回 缓冲区数据抛弃 直接发送RST结束命令到对方 并无需经过2MSL等待
         * true 200: 关闭时最长阻塞200毫秒 随后按第二种情况处理
         */
        socket.setSoLinger(true, 200);

        // 是否让紧急数据内敛 默认false; 紧急数据通过: socket.sendUrgentData(1)
//        socket.setOOBInline(true);

        // 设置接收发送缓冲器大小
        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        // 设置性能参数 短连接 延迟 带宽的相对重要性
        socket.setPerformancePreferences(1, 1, 1);
    }

    private static void todo(Socket socket) throws IOException {
        // 得到socket输出流
        OutputStream outputStream = socket.getOutputStream();
        // 得到socket输入流
        InputStream inputStream = socket.getInputStream();

        byte[] buffer = new byte[256];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        byteBuffer.put((byte) 126);
        char c = 'a';
        byteBuffer.putChar(c);
        int i = 123321;
        byteBuffer.putInt(i);
        boolean b = true;
        byteBuffer.put((byte) (b ? 1 : 0));
        long l = 1253586585;
        byteBuffer.putLong(l);
        float f = 12.345f;
        byteBuffer.putFloat(f);
        double d = 12.353262323;
        byteBuffer.putDouble(d);
        String str = "abcdefg你好";
        byteBuffer.put(str.getBytes());

        // 发送到服务器
        outputStream.write(buffer, 0, byteBuffer.position() + 1);

        // 接收服务器返回
        int read = inputStream.read(buffer);
        System.out.println("收到数量: " + read);

        // 资源释放
        outputStream.close();
        inputStream.close();
    }
}
