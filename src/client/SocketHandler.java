package client;

import client.constants.CLIENT_CONSTANTS;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketHandler {
    Socket socket;
    OutputStream outputStream;
    InputStream inputStream;
    private BufferedReader reader;
    private ObjectOutputStream obj;

    //user
    private String id;
    private List<String> msgArr;
    private String msg;

    SocketHandler() throws IOException {
        this.socket = new Socket("localhost" , 8091);
        this.outputStream = socket.getOutputStream();
        this.obj = new ObjectOutputStream(outputStream);
        this.inputStream = socket.getInputStream();
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.msgArr = new ArrayList<>();

        //응답 받는 쓰레드 실행
        startListeningThread();
    }

    //로그인
    public void login(String id) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        map.put("command", "login");
        map.put("id", id);
        setId(id);

        requestData.add(map);

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
    }

    //로그아웃 (소켓종료)
    public void logout(String id) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        map.put("command", "socketEnd");
        map.put("id", id);

        requestData.add(map);

        Socket socket = new Socket("localhost" , 8091);
        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
        socket.close();
    }

    //파일 업로드
    public void fileUpload(String id, File[] files) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> infoMap = new HashMap<>();

        //정보 담는 맵
        infoMap.put("command", "sendFile");
        infoMap.put("id", id);
        requestData.add(infoMap);

        //파일 담는 맵
        for (File f : files) {
            Map<String, Object> fileMap = new HashMap<>();

            fileMap.put("fileName", f.getName());
            fileMap.put("fileByte", fileToByteArray(f));

            requestData.add(fileMap);
        }

        Socket socket = new Socket("localhost" , 8091);
        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
        socket.close();
    }

    //파일 수정
    public void fileModify(String id, File[] files) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> infoMap = new HashMap<>();

        //정보 담는 맵
        infoMap.put("command", "modifyFile");
        infoMap.put("id", id);
        requestData.add(infoMap);

        //파일 담는 맵
        for (File f : files) {
            Map<String, Object> fileMap = new HashMap<>();

            fileMap.put("fileName", f.getName());
            fileMap.put("fileByte", fileToByteArray(f));

            requestData.add(fileMap);
        }

        System.out.println("파일 수정 데이터 : "+requestData);
        Socket socket = new Socket("localhost" , 8091);
        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
        socket.close();
    }

    //삭제
    public void fileDelete(String id, String deleteFileName) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> infoMap = new HashMap<>();

        //정보 담는 맵
        infoMap.put("command", "deleteFile");
        infoMap.put("id", id);
        requestData.add(infoMap);

        //파일 담는 맵
        Map<String, Object> fileMap = new HashMap<>();

        fileMap.put("fileName", deleteFileName);
        fileMap.put("fileByte", null);

        requestData.add(fileMap);

        System.out.println("파일 삭제 데이터 : "+requestData);
        Socket socket = new Socket("localhost" , 8091);
        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
        socket.close();
    }

    // 다른 클라이언트와 파일 단순 공유
    public void shareFile(String id, File[] files, String targetId) throws IOException {
        List<Map<String, Object>> requestData = new ArrayList<>();
        Map<String, Object> infoMap = new HashMap<>();

        //정보 담는 맵
        infoMap.put("command", "shareFile");
        infoMap.put("id", id);
        infoMap.put("targetId", targetId);
        requestData.add(infoMap);

        //파일 담는 맵
        for (File f : files) {
            Map<String, Object> fileMap = new HashMap<>();

            fileMap.put("fileName", f.getName());
            fileMap.put("fileByte", fileToByteArray(f));

            requestData.add(fileMap);
        }

        Socket socket = new Socket("localhost" , 8091);
        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

        obj.writeObject(requestData);
        obj.flush(); // 데이터를 즉시 전송
        socket.close();
    }

    //텍스트 데이터 수신 데몬
    public void startListeningThread() throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        List<Map<String, Object>> data = (ArrayList<Map<String, Object>>) objectInputStream.readObject();

                        if (data != null){
                            for (Map<String, Object> map : data) {
                                if (map.containsKey("string")) {
                                    msgArr.add( map.get("string").toString() );
                                } else {
                                    saveFile(data);
                                    break;
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    //파일 저장
    public void saveFile(List<Map<String, Object>> data){
        String fileName = null;
        for (Map<String, Object> saveList : data) {
            fileName = (String) saveList.get("fileName");

            byte[] bytes = (byte[]) saveList.get("fileByte");

            try {
                //사용자의 개인 Storage(파일경로)에 파일을 저장
                FileOutputStream fileOutputStream = new FileOutputStream(CLIENT_CONSTANTS.CLIENT_PATH +getId()+"/"+fileName);
                fileOutputStream.write(bytes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //File to byte[]
    public byte[] fileToByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            // 파일을 읽기 위한 FileInputStream 생성
            fileInputStream = new FileInputStream(file);

            // 파일 크기만큼의 byte 배열 생성
            bytesArray = new byte[(int) file.length()];

            // 파일 데이터를 byte 배열로 읽기
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // FileInputStream 닫기
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }

    //Get, Set
    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getMsgArr() {
        return msgArr;
    }

    public void setMsgArr(List<String> msgArr) {
        this.msgArr = msgArr;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

}
