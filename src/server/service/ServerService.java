package server.service;


import server.constants.SERVER_CONSTANTS;
import server.info.FileLog;
import server.info.LoginInfo;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * 서버에서 실행할 로직 , SERVICE
 */
public class ServerService {

    Logger logger;
    LoginInfo loginInfo;
    FileLog fileLog;
    List<Map<String, Object>> socketList;
    String id;
    Socket clientSocket;
    Socket targetSocket;

    List<Map<String, Object>> targetList;

    String msg;
    PrintWriter writer;

    LocalDateTime localDateTime;
    DateTimeFormatter formatter;

    String nowTime;

    ObjectOutputStream obj;

    /**
     * 생성자
     *
     * @param loginInfo
     */
    public ServerService(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
        this.fileLog = new FileLog();
        this.logger = Logger.getLogger("server.service.ServerService");
    }


    /**
     * 로그인 후 접속
     */
    public void login(String id, Socket socket) {
        loginInfo.setLoginInfo(id, socket);
        socketList = loginInfo.getLoginInfo();
        sendAllMessage(id , id + " 님이 접속 했습니다.");
        logger.info(id + "접속 완료");
    }



    /**
     * 서버에 파일 저장 실행
     */
    public void saveFile(String id, List<Map<String, Object>> data) {
        //기존에 먼저 파일을 올린적이 있는지 검사
        //이름으로 파일
        List<Map<String, String>> fileList = fileLog.getFileList();

        if (fileList.size() > 0) {
            saveProccess(id, fileList, data);
        } else {
            // 바로 저장
            saveProccess(id, fileList, data);
        }

    }


    /**
     * 데이터 저장 로직
     *
     * @param id
     * @param data
     */
    public void saveProccess(String id, List<Map<String, String>> fileList, List<Map<String, Object>> data) {
        String msg = null;
        String fileName = null;
        for (Map<String, Object> saveList : data) {
            boolean checkResult = checkFile((String) saveList.get("fileName"), fileList);
            fileName = (String) saveList.get("fileName");
            if (checkResult) {
                // 저장 로직
                byte[] bytes = (byte[]) saveList.get("fileByte");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(SERVER_CONSTANTS.SERVER_PATH + fileName);
                    fileOutputStream.write(bytes);
                    fileLog.setFileList(id, fileName);

                    msg = "가" + fileName + "을 업로드 하였습니다.";
                    sendAllMessage(id, msg);
                    logger.info(fileName + "업로드 완료");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                msg = fileName + "충돌로 저장에 실패했습니다.";
                sendAllMessage(id, msg);
                logger.info(fileName + "충돌 발생");
            }
        }
    }


    /**
     * 서버 파일 수정
     */
    public void modifyFile(String id, List<Map<String, Object>> data) {
        String fileName = null;
        for (Map<String, Object> updateList : data) {
            fileName = (String) updateList.get("fileName");
            byte[] bytes = (byte[]) updateList.get("fileByte");
            File file = new File(SERVER_CONSTANTS.SERVER_PATH + fileName);

            if (file.exists()) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(SERVER_CONSTANTS.SERVER_PATH + fileName);
                    fileOutputStream.write(bytes);
                    fileLog.setFileList(id, fileName);
                    msg = "가" + fileName + "을 수정하였습니다.";
                    sendAllMessage(id, msg);
                    logger.info(fileName + " 수정 완료");

                    sendModifyFile(fileName, bytes);
                    //수정 목록 만들기
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                sendMessage(id, id + " 가 수정하려는" + fileName + "파일은 서버에 없습니다.");
                logger.info(fileName + "이/가 없어 수정 실패");
            }
        }
    }


    /**
     * 수정된 파일 CLIENT에게 전송
     *
     * @param fileName
     * @param bytes
     */
    public void sendModifyFile(String fileName, byte[] bytes) {
        List<Map<String, Object>> infoData = loginInfo.getSendInfo();
        Socket sendSocket = null;
        String sendId = null;
        for (Map<String, Object> info : infoData) {
            if (fileName.equals(info.get("fileName"))) {
                List<Map<String, Object>> sendList = new ArrayList<>();
                Map<String, Object> sendMap = new HashMap<>();
                sendMap.put("fileName", fileName);
                sendMap.put("fileByte", bytes);
                sendList.add(sendMap);

                sendSocket = (Socket) info.get("socket");
                sendId = (String) info.get("id");
                //전송하는 object 전송하는 로직 실행
                sendTargetObject(sendSocket, sendList);
                //메세지 전송하는 로직 실행
                sendMessage(sendId, "sendId 님이 파일을 업데이트하여 전송되었습니다.");
                logger.info("파일 업데이트로 " + sendId + " 에게 파일 전송");
            }
        }
    }


    /**
     * 서버 파일 삭제
     */
    public void deleteFile(String id, List<Map<String, Object>> data) {

        String fileName = null;
        for (Map<String, Object> updateList : data) {
            fileName = (String) updateList.get("fileName");
            File file = new File(SERVER_CONSTANTS.SERVER_PATH + fileName);
            if (file.exists()) {
                try {
                    file.delete();
                    fileLog.deleteFileList(fileName);
                    msg = "가" + fileName + "을 삭제하였습니다.";
                    sendAllMessage(id, msg);
                    logger.info(fileName + " 삭제 완료");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sendMessage(id, id + " 가 삭제하려는" + fileName + "파일은 서버에 없습니다.");
                logger.info(fileName + " 삭제 실패");
            }
        }
    }


    /**
     * 파일 전송
     *
     * @param id
     * @param targetId
     * @param data
     */
    public void shareFile(String id, String targetId, List<Map<String, Object>> data) {

        //접속한 인원이 있는지 없는지 검사
        this.targetSocket = loginInfo.findTarget(targetId);

        if (this.targetSocket == null) {
            sendMessage(id, id + " 가 전송하려는 대상" + "[ " + targetId + " ] 가 접속하지 않았습니다.");
        } else {
            List<Map<String, String>> fileList = fileLog.getFileList();
            shareProccess(id, targetId, targetSocket, fileList, data);
        }
    }


    /**
     * 파일 전송 프로세스
     *
     * @param id
     * @param targetSocket
     * @param fileList
     * @param data
     */
    public void shareProccess(String id, String targetId, Socket targetSocket, List<Map<String, String>> fileList, List<Map<String, Object>> data) {

        String msg = null;
        String fileName = null;
        List<Map<String, Object>> sendTargetList = new ArrayList<>(); // 전송 데이터 목록
        for (Map<String, Object> saveList : data) {
            boolean checkResult = checkFile((String) saveList.get("fileName"), fileList);
            fileName = (String) saveList.get("fileName");
            if (checkResult) {
                // 저장 로직
                byte[] bytes = (byte[]) saveList.get("fileByte");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(SERVER_CONSTANTS.SERVER_PATH + fileName);
                    fileOutputStream.write(bytes);
                    fileLog.setFileList(id, fileName);

                    logger.info(fileName + "업로드 완료");

                    msg = fileName + "전송 과정 중 서버 업로드 성공했습니다.";
                    sendMessage(id, msg);

                    Map<String, Object> targetData = new HashMap<>();
                    targetData.put("fileName", fileName);
                    targetData.put("fileByte", bytes);
                    sendTargetList.add(targetData);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                msg = fileName + " 전송 과정 중 파일 충돌이 발생 했습니다.";
                sendAllMessage(id, msg);
                logger.info(fileName + "충돌 발생");
            }
        }
        if (sendTargetList.size() > 0) {
            // object로 전송
            Socket target = loginInfo.findTarget(targetId);
            sendTargetObject(target, sendTargetList);
            sendMessage(targetId, id + " 가 파일을 전송하였습니다.");
            sendAllMessage(id, "파일 전송에 성공 하였습니다.");
            logger.info("파일 전송 완료");
            //targetId , targetSocket , 파일명
            loginInfo.setSendInfo(targetId, target, sendTargetList);// 전송 리스트에 데이터 넣기
        } else {
            logger.info("파일 충돌로 전송된 파일이 없습니다.");
        }

    }


    /**
     * 소켓 접속 종료
     *
     * @param id
     */
    public void socketOut(String id) {
        String msg = id + " 님이 접속을 종료 하였습니다.";
        sendAllMessage(id, msg);
        loginInfo.deleteLoginInfo(id);  // 접속 목록 삭제
        loginInfo.deleteSendInfo(id);   // 파일 전송 목록 삭제
    }


    /**
     * 기존 파일 log 검사
     */
    public boolean checkFile(String fileName, List<Map<String, String>> data) {
        String saveFileName = null;
        for (Map<String, String> dataMap : data) {
            saveFileName = dataMap.get("fileName");
            if (fileName.equals(saveFileName)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 클라이언트 전송 메세지 생성
     *
     * @return
     */
    public List<Map<String , Object>> makeMsg(String text) {
        localDateTime = LocalDateTime.now();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        nowTime = localDateTime.format(formatter);
        StringBuffer str = new StringBuffer();
        str.append("[ ");
        str.append(nowTime);
        str.append(" ]");
        str.append("  ");
        str.append(text);

        List<Map<String , Object>> list = new ArrayList<>();
        Map<String , Object> map = new HashMap<>();
        map.put("string" , str);
        list.add(map);

        return list;
    }

    /**
     * 클라이언트 메세지 전송
     */
    public void sendMessage(String id, String msg) {
        Socket socketId = loginInfo.findTarget(id);
        try {
            obj = new ObjectOutputStream(socketId.getOutputStream());
            obj.writeObject(makeMsg(msg));
            obj.flush();
//            writer = new PrintWriter(socketId.getOutputStream());
//            writer.println(makeMsg(msg));
//            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * client에게 파일 전송
     *
     * @param targetData
     */
    public void sendTargetObject(Socket socket, List<Map<String, Object>> targetData) {
        try {
            obj = new ObjectOutputStream(socket.getOutputStream());
            obj.writeObject(targetData);
            obj.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 모든 사용자에게 메세지 전송
     * @param msg
     */
    public void sendAllMessage(String id, String msg) {
        socketList = loginInfo.getLoginInfo();
        for (Map<String, Object> data : socketList) {
            Socket socket = (Socket) data.get("socket");
            try {
                obj = new ObjectOutputStream(socket.getOutputStream());
                obj.writeObject(makeMsg(msg));
                obj.flush();
//                writer = new PrintWriter(socket.getOutputStream());
//                writer.println(makeMsg(msg));
//                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }


}