package com.yoxiang.io.nio2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ExecutionException;

/**
 * Author: Rivers
 * Date: 2017/7/29 08:33
 */
public class Nio2Client {

    private AsynchronousSocketChannel channel;
    private CharBuffer charBuffer;
    private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
    private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

    public void init() throws IOException, ExecutionException, InterruptedException {

        channel = AsynchronousSocketChannel.open();
        if (channel.isOpen()) {
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            Void connect = channel.connect(new InetSocketAddress("127.0.0.1", 8080)).get();

            if (connect != null) {
                throw new RuntimeException("链接服务器失败！");
            }
        } else {
            throw new RuntimeException("通道未打开！");
        }
    }

    public void start() throws IOException, ExecutionException, InterruptedException {

        System.out.printf("请输入客户端请求：");
        String request = clientInput.readLine();

        channel.write(ByteBuffer.wrap(request.getBytes())).get();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer).get() != -1) {
            buffer.flip();
            charBuffer = decoder.decode(buffer);
            String response = charBuffer.toString().trim();
            System.out.println("服务端响应：" + response);

            if (buffer.hasRemaining()) {
                buffer.compact();
            } else {
                buffer.clear();
            }

            request = clientInput.readLine();
            channel.write(ByteBuffer.wrap(request.getBytes())).get();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        Nio2Client client = new Nio2Client();
        client.init();
        client.start();
    }
}
