package client;

import client.gui.Login;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        SocketHandler socketHandler = new SocketHandler();

        // GUI 생성
        Login login = new Login(socketHandler);
    }
}