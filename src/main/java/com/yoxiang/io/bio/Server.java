package com.yoxiang.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Author: Rivers
 * Date: 2017/7/28 21:50
 */
public class Server {

    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(8080);
        Socket socket = server.accept();

        BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        System.out.println("客户端请求为：" + reader.readLine());

        String input = serverInput.readLine();
        while (!input.equals("exit")) {
            writer.print(input);
            writer.flush();
            System.out.println("客户端请求为：" + reader.readLine());
            input = serverInput.readLine();
        }

        reader.close();
        writer.close();
        socket.close();
        server.close();
    }
}
