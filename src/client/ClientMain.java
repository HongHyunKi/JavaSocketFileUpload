package client;

import client.gui.Login;
import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        SocketHandler socketHandler = new SocketHandler();

        // 로그인 GUI 생성
        Login login = new Login(socketHandler);
    }
}