package com.yoxiang.io.nio2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Author: Rivers
 * Date: 2017/7/29 09:41
 */
public class Nio2Server2 {

    private AsynchronousServerSocketChannel serverChannel;

    class ServerCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        private AsynchronousServerSocketChannel channel;
        private ByteBuffer buffer = ByteBuffer.allocate(1024);
        private CharBuffer charBuffer;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        public ServerCompletionHandler(AsynchronousServerSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(AsynchronousSocketChannel result, Void attachment) {

            serverChannel.accept(null, this);
            try {
                while (result.read(buffer).get() != -1) {
                    buffer.flip();
                    charBuffer = decoder.decode(buffer);
                    String request = charBuffer.toString().trim();
                    System.out.println("客户端请求：" + request);
                    ByteBuffer outBuffer = ByteBuffer.wrap(request.getBytes());
                    result.write(outBuffer).get();
                    if (buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex);
            } finally {
                try {
                    result.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {

            serverChannel.accept(null, this);
            throw  new RuntimeException("链接失败！");
        }
    }

    public void init() throws IOException {

        serverChannel = AsynchronousServerSocketChannel.open();
        if (serverChannel.isOpen()) {
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
        } else {
            throw new RuntimeException("通道未打开！");
        }
    }

    public void start() throws InterruptedException {

        System.out.println("等待客户端请求：");
        serverChannel.accept(null, new ServerCompletionHandler(serverChannel));
        while (true) {
            Thread.sleep(5000);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        Nio2Server2 server = new Nio2Server2();
        server.init();
        server.start();
    }
}
