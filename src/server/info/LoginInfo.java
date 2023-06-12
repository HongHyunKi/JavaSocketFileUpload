package server.info;

import java.net.Socket;
import java.util.*;

public class LoginInfo {

    // key : id , pw socketInfo
    /**
     * 사용자 전체 정보 List
     */
    private List<Map<String, Object>> loginInfo = new ArrayList<>();

    /**
     * 전송한 사용자들의 정보
     */
    private List<Map<String, Object>> sendInfo = new ArrayList<>();

    public List<Map<String, Object>> getSendInfo() {
        return sendInfo;
    }

    public void setSendInfo(String id, Socket socket, List<Map<String, Object>> data) {
        for (Map<String, Object> dataMap : data) {
            dataMap.put("targetId", id);
            dataMap.put("socket", socket);
//            fileName
//            fileByte
            sendInfo.add(dataMap);
        }
    }


    public void deleteSendInfo(String id) {
        for (int i = 0; i < sendInfo.size(); i++) {
            if (sendInfo.get(i).get("id").equals(id)) {
                sendInfo.remove(i);
            }
        }
    }


    public List<Map<String, Object>> getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(String id, Socket socket) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("socket", socket);
        loginInfo.add(data);
    }

    public void deleteLoginInfo(String id) {
        for (int i = 0; i < loginInfo.size(); i++) {
            if (loginInfo.get(i).get("id").equals(id)) {
                loginInfo.remove(i);
            }
        }
    }

    public Socket findTarget(String targetId) {
        for (Map<String, Object> data : loginInfo) {
            if (targetId.equals(data.get("id"))) {
                return (Socket) data.get("socket");
            }
        }
        return null;
    }


}