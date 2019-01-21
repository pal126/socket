package com.pal.udp.client;

import com.pal.udp.client.bean.ServerInfo;
import com.pal.udp.constants.UDPConstants;
import com.pal.udp.utils.ByteUtils;
import com.pal.udp.utils.MessageCreator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher Started");

        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // finish
        System.out.println("UDPSearcher Finished");
        if (listener == null) {
            return null;
        }
        List<ServerInfo> devices = listener.getServerInfosAndClose();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started");

        // 搜索方由系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建请求数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        // CMD命令
        byteBuffer.putShort((short) 1);
        // 回送端口信息
        byteBuffer.putInt(LISTEN_PORT);
        // 构建packet
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position() + 1);
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置服务器端口
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        // 发送
        ds.send(requestPacket);
        ds.close();
        System.out.println("UDPSearcher sendBroadcast finished");
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfos = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            super();
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();

            // 通知已启动
            startDownLatch.countDown();
            try {
                // 监听回送端口
                ds = new DatagramSocket(listenPort);

                // 构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen
                            && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    System.out.println("UDPSearcher receive from ip:" + ip + " port" + port + " isValid" + isValid);

                    if (!isValid) {
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0) {
                        System.out.println("UDPSearcher receive cmd:" + cmd);
                        continue;
                    }

                    String sn = new String(buffer, minLen, dataLen - minLen);
                    ServerInfo serverInfo = new ServerInfo(serverPort, ip, sn);
                    serverInfos.add(serverInfo);
                    // 成功接收到一份
                    receiveDownLatch.countDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
            System.out.println("UDPSearcher receive finished");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerInfosAndClose() {
            done = true;
            close();
            return serverInfos;
        }
    }
}
