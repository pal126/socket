package com.pal.socket;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;

public class Server {

    private static final int PORT = 20000;

    public static void main(String[] args) throws IOException {
        ServerSocket server = createServerSocket();

        initServerSocket(server);

        // 绑定到本地端口 允许等待的连接队列50个
        server.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("服务器已启动: " + server.getInetAddress() + "port: " + server.getLocalPort());

        // 等待客户端连接
        for (; ; ) {
            // 获取客户端
            Socket client = server.accept();
            // 客户端构建异步线程
            ClinetHandler clinetHandler = new ClinetHandler(client);
            // 启动线程
            clinetHandler.start();
        }

    }

    private static ServerSocket createServerSocket() throws IOException {
        // 创建基础的ServerSocket
        ServerSocket serverSocket = new ServerSocket();
        return serverSocket;
    }

    private static void initServerSocket(ServerSocket server) throws SocketException {
        // 是否复用未完全关闭的地址端口
        server.setReuseAddress(true);

        // 设置接收发送缓冲器大小 在server.accept()之前设置
        server.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置server.accept()超时时间
        // server.setSoTimeout(2000);

        // 设置性能参数 短连接 延迟 带宽的相对重要性
        server.setPerformancePreferences(1, 1, 1);
    }

    /**
     * 客户端消息处理
     */
    private static class ClinetHandler extends Thread {
        private Socket socket;

        ClinetHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接: " + socket.getInetAddress() + "prot: " + socket.getPort());

            try {
                // 获取socket流
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                byte[] buffer = new byte[256];
                int read = inputStream.read(buffer);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);

                // byte
                byte by = byteBuffer.get();

                // char
                char c = byteBuffer.getChar();

                // int
                int i = byteBuffer.getInt();

                // boolean
                boolean b = byteBuffer.get() == 1;

                // long
                long l = byteBuffer.getLong();

                // float
                float f = byteBuffer.getFloat();

                // double
                double d = byteBuffer.getDouble();

                // string
                int pos = byteBuffer.position();
                String string = new String(buffer, pos, read - pos - 1);

                System.out.println("收到数量: " + read + "data: "
                        + by + "\n"
                        + c + "\n"
                        + i + "\n"
                        + b + "\n"
                        + l + "\n"
                        + f + "\n"
                        + d + "\n"
                        + string + "\n");

                outputStream.write(buffer, 0, read);

                // 资源释放
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                System.out.println("连接异常断开");
            } finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已关闭: " + socket.getInetAddress() + "prot: " + socket.getPort());
        }
    }
}
