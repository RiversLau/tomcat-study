package com.yoxiang.io.nio2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Author: Rivers
 * Date: 2017/7/29 09:29
 */
public class Nio2Client2 {

    class ClientCompletionHandler implements CompletionHandler<Void, Void> {

        private AsynchronousSocketChannel channel;
        private CharBuffer charBuffer = null;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

        ClientCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Void result, Void attachment) {

            try {
                System.out.println("输入客户端请求：");
                String request = clientInput.readLine();
                channel.write(ByteBuffer.wrap(request.getBytes())).get();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (channel.read(buffer).get() != -1) {
                    buffer.flip();
                    charBuffer = decoder.decode(buffer);
                    System.out.println(charBuffer.toString());
                    if (buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                    request = clientInput.readLine();
                    channel.write(ByteBuffer.wrap(request.getBytes())).get();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {

            throw new RuntimeException("链接服务器失败！");
        }
    }

    public void start() throws IOException, InterruptedException {

        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        if (channel.isOpen()) {
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            channel.connect(new InetSocketAddress("127.0.0.1", 8080), null, new ClientCompletionHandler(channel));
            while (true) {
                Thread.sleep(5000);
            }
        } else {
            throw new RuntimeException("通道未打开！");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Nio2Client2 client = new Nio2Client2();
        client.start();
    }
}
