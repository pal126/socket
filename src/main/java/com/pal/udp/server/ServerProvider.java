package com.pal.udp.server;

import com.pal.udp.constants.UDPConstants;
import com.pal.udp.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        // 存储消息的buffer
        final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvider started");

            try {
                // 监听端口
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    ds.receive(receivePack);
                    // 打印接收到的消息与发送者的信息
                    // 发送者的ip地址
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("ServerProvider receive from ip: " + clientIp +
                            "\tport: " + clientPort + "\tdataValid: " + isValid);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }

                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index++] & 0xff)));

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responserPacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responserPacket);

                        System.out.println("ServerProvider response to: " + clientIp + "\tport:" +
                                responsePort + "\tdatalength: " + len);
                    } else {
                        System.out.println("ServerProvider receive cmd unsupport, cmd: " + cmd +
                                "\tport: " + port);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }

            // finish
            System.out.println("UDPProvider Finished");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
