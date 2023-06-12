package client;

import java.io.File;

public class FileStorage {
    private String storagePath;

    public FileStorage(String storagePath) {
        this.storagePath = storagePath;
    }

    public void initialize() {
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created) {
                System.out.println("사용자 폴더 생성 실패");
                // 디렉토리 생성에 실패한 경우 예외 처리 또는 오류 메시지 표시
            }
        }
    }

    public File getFile(String filename) {
        return new File(storagePath, filename);
    }
}