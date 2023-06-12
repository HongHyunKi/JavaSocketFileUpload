package client;
import java.net.Socket;
import java.io.Serializable;

//소켓으로 Class를 전송하려면 해당 클래스가 직렬화 가능해야 함
public class Info implements Serializable {
    private static final long serialVersionUID = 1L; //직렬화 버전관리

    private String order;
    private String id;
    private byte[] fileData;

    public Info(String order, String id, byte[] fileData) {
        this.order = order;
        this.id = id;
        this.fileData = fileData;
    }

    @Override
    public String toString(){
        return " order:" + order + " id:" + id + " file:" + fileData;
    }

    //get, set
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public byte[] getFileData() {
        return fileData;
    }

}