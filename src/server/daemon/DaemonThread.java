package server.daemon;

import server.info.LoginInfo;

import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

public class DaemonThread extends Thread{

    LoginInfo loginInfo;

    Logger logger;

    int members;
    /**
     * 생성자
     */
    public DaemonThread(LoginInfo loginInfo){
        this.loginInfo = loginInfo;
        this.logger = Logger.getLogger("src.service.DaemonThread");
        logger.info("접속 인원 검사 시작");
        this.members =  loginInfo.getLoginInfo().size();
    }

    /**
     * 접속 끊어진 인원 지속적으로 검사
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {


                throw new RuntimeException(e);
            }

            List<Map<String, Object>> clientList = loginInfo.getLoginInfo();
            String id = null;
            for (Map<String, Object> list : clientList) {
                id = (String) list.get("id");
                Socket socket = (Socket) list.get("socket");
                if (socket.isClosed()) {
                    loginInfo.deleteLoginInfo(id);
                    logger.info(id + "님 과의 연결이 끊어졌습니다.");
                }
            }
            if (members != clientList.size()) {
                logger.info("현재 접속 인원 " + "[ " + clientList.size() + " ]");
                this.members = clientList.size();
            }
        }
    }
}