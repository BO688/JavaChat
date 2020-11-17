package com.company.BIO.TCP.server;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAcceptBase64 extends Thread{
    private static ServerSocket ssA;
    private static ServerSocket ssS;
    static {
        try {
            ssA = new ServerSocket(12345);
            ssS = new ServerSocket(20202);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"12345端口和20202端口被占用");
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while (true) {
            try  {
                Socket s = ssA.accept();
                DataInputStream is = new DataInputStream(s.getInputStream()); DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                String temp = is.readUTF();
                String[] ip_length = temp.split("&&");
                String ip = ip_length[0];
                String length = ip_length[1];
                String message;
                String base64 = "";
                try {
                    while (Integer.parseInt(length) != base64.length()) {
                        message = is.readUTF();
                        base64 += message;
                    }
                } catch (EOFException eof) {
                    eof.printStackTrace();
                }
                dos.writeUTF("ok");
                new ServerSendBase64(ip, base64, ssS).start();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

    }
}
