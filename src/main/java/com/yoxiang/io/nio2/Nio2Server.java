package com.yoxiang.io.nio2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.*;

/**
 * Author: Rivers
 * Date: 2017/7/29 08:49
 */
public class Nio2Server {

    private ExecutorService taskExecutor;
    private AsynchronousServerSocketChannel serverChannel;

    class Worker implements Callable<String> {

        private CharBuffer charBuffer;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        private AsynchronousSocketChannel channel;

        Worker(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public String call() throws Exception {

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (channel.read(buffer).get() != -1) {
                buffer.flip();
                charBuffer = decoder.decode(buffer);
                String request = charBuffer.toString().trim();
                System.out.println("客户端请求：" + request);

                ByteBuffer outBuffer = ByteBuffer.wrap("请求收到".getBytes());
                channel.write(outBuffer);
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                }
            }
            channel.close();
            return "OK";
        }
    }

    public void init() throws IOException {

        taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        serverChannel = AsynchronousServerSocketChannel.open();
        if (serverChannel.isOpen()) {
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
        } else {
            throw new RuntimeException("通道未打开！");
        }
    }

    public void start() {

        System.out.println("等待客户端请求...");
        while (true) {
            Future<AsynchronousSocketChannel> future = serverChannel.accept();
            try {
                AsynchronousSocketChannel channel = future.get();
                taskExecutor.submit(new Worker(channel));
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("服务器关闭！");
                taskExecutor.shutdown();
                while (!taskExecutor.isTerminated()) {
                }
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {

        Nio2Server server = new Nio2Server();
        server.init();
        server.start();
    }
}
