package server.info;

import java.util.*;

public class FileLog {


    /**
     * 파일 전체 List
     */
    private List<Map<String , String >> fileList = new ArrayList<>();

    private String id;

    private String fileName;

    /**
     * save file Map
     * @param id
     * @param fileName
     */
    public void setFileList(String id , String fileName){
        Map<String , String > data = new HashMap<>();
        data.put("id" , id);
        data.put("fileName" , fileName);

        fileList.add(data);
    }

    /**
     * get file Map
     * @return
     */
    public List<Map<String , String>> getFileList(){
        return fileList;
    }

    public void deleteFileList(String fileName){
        for(int i =0; i< this.fileList.size() ; i++){
            if(fileList.get(i).get("fileName").equals(fileName)){
                fileList.remove(i);
            }
        }
    }
}