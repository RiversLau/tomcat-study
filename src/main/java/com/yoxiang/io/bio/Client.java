package com.yoxiang.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Author: Rivers
 * Date: 2017/7/28 21:43
 */
public class Client {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 8080);
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

        PrintWriter write = new PrintWriter(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String input = clientInput.readLine();
        while (!input.equals("exit")) {
            write.println(input);
            write.flush();
            System.out.println("服务端响应为：" + reader.readLine());
            input = clientInput.readLine();
        }

        write.close();
        reader.close();
        socket.close();
    }
}
