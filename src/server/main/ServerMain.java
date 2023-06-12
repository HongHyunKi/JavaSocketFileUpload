package server.main;

import server.daemon.DaemonThread;
import server.info.LoginInfo;
import server.service.ServerService;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.util.*;
import java.util.logging.Logger;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(8091);
        LoginInfo loginInfo = new LoginInfo();
        Thread daemon = new Thread(new DaemonThread(loginInfo));
        daemon.start();
        ServerService service = new ServerService(loginInfo);
        Logger logger = Logger.getLogger("src.server.Main");


        // trying socket
        while (true) {

            Socket clientSocket = serverSocket.accept();    // 소켓 연결 받기 상태
            InputStream inputStream = clientSocket.getInputStream();    //데이터 입력
            OutputStream outputStream = clientSocket.getOutputStream();
            //클라이언트가 전송한 오브젝트를 읽어들인다.
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            //오브젝트 list를 map으로 받기
            List<Map<String, Object>> objectList = (ArrayList<Map<String, Object>>) objectInputStream.readObject();

            String command = null;      // 명령
            String id = null;           // 아이디
            String targetId = null;
            List<Map<String, Object>> fileData = new ArrayList<>();        //전송 파일 데이터


            for (Map<String, Object> data : objectList) {
                if (data.containsKey("command")) {
                    command = (String) data.get("command");
                    id = (String) data.get("id");
                }
                if (data.containsKey("targetId")) {
                    targetId = (String) data.get("targetId");
                }
                if (data.containsKey("fileName")) {
                    fileData.add(data);
                }
            }

            switch (command) {
                case "login": // 접속
                    logger.info("LOGIN ID " + id);
                    service.login(id, clientSocket);
                    break;
                case "sendFile": //파일 저장
                    logger.info("[ SAVE FILE ] USER : " + id);
                    service.saveFile(id, fileData);
                    break;
                case "modifyFile": //파일 수정
                    logger.info("[ MODIFY FILE ] USER : " + id);
                    service.modifyFile(id, fileData);
                    break;
                case "deleteFile": //파일 삭제
                    logger.info("[ DELETE FILE ] USER : " + id);
                    service.deleteFile(id, fileData);
                    break;
                case "socketEnd":
                    logger.info("[ SOCKET END ] USER : " + id);
                    service.socketOut(id);
                    break;
                case "shareFile":
                    logger.info("[ SHARE FILE ] USER : " + id);
                    service.shareFile(id, targetId, fileData);

            }
        }
    }
}