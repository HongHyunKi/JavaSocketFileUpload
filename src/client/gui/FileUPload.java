package client.gui;

import client.FileStorage;
import client.SocketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class FileUPload extends JFrame {
    private JTextField filePathField;
    private JTextArea messageArea;
    private JList<String> fileList;
    private File[] selectedFiles;

    SocketHandler socketHandler;

    public FileUPload(SocketHandler socketHandler, FileStorage fileStorage) {
        this.socketHandler = socketHandler;
        receiveMessage(); //데몬 실행

        setTitle("파일 업로드 및 다운로드");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 파일 경로 필드
        filePathField = new JTextField();
        filePathField.setEditable(false);

        // 파일 선택 버튼
        JButton selectButton = new JButton("파일 선택");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                int result = fileChooser.showOpenDialog(FileUPload.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    for (File file : selectedFiles) {
                        uploadFile(socketHandler.getId(), file);
                    }
                    refreshFileList();
                }
            }
        });

        // 업로드된 파일 리스트
        selectedFiles = new File[0];
        fileList = new JList<>();
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addListSelectionListener(e -> {
            int selectedIndex = fileList.getSelectedIndex();
            if (selectedIndex >= 0) {
                File selectedFile = selectedFiles[selectedIndex];
                filePathField.setText(selectedFile.getAbsolutePath());
            } else {
                filePathField.setText("");
            }
        });

        // 파일 업로드 버튼
        JButton uploadButton = new JButton("파일 업로드");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //업로드 버튼 클릭 시 선택된 파일 서버에 전송
                String id = socketHandler.getId();

                //선택된 파일이 없으면 알람
                if(selectedFiles.length == 0){
                    JOptionPane.showMessageDialog(null, "선택된 파일이 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                //파일 업로드
                try {
                    socketHandler.fileUpload(id, selectedFiles);

                    //파일 리스트 초기화 및 재렌더링
                    resetSelectedFiles();
                    refreshFileList();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 수정 버튼
        JButton editButton = new JButton("수정");
        editButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //선택된 파일 서버에 전송
                String id = socketHandler.getId();

                if(selectedFiles.length == 0){
                    JOptionPane.showMessageDialog(null, "선택된 파일이 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                //파일 업로드
                try {
                    socketHandler.fileModify(id, selectedFiles);

                    //파일 리스트 초기화 및 재렌더링
                    resetSelectedFiles();
                    refreshFileList();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 파일 삭제 버튼
        JButton deleteButton = new JButton("파일 삭제");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //모달 표시
                delSendWindow();
            }
        });

        // 동기화 버튼
//        JButton syncButton = new JButton("동기화");
//        syncButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                try {
//                    socketHandler.synchronizationFile(socketHandler.getId());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//
//            }
//        });

        // 개인 전송 버튼
        JButton sendButton = new JButton("보내기");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 파일 보내기 작업 수행
                createSendWindow();
            }
        });

        // 종료 버튼
        JButton exitButton = new JButton("종료");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    socketHandler.logout(socketHandler.getId());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.exit(0);
            }
        });

        //여기넣음
        // 파일 리스트 스크롤 패널
        JScrollPane fileScrollPane = new JScrollPane(fileList);

        // 파일 리스트 패널
        JPanel fileListPanel = new JPanel();
        fileListPanel.setLayout(new BorderLayout());
        fileListPanel.add(new JLabel("선택된 파일 리스트:"), BorderLayout.NORTH);
        fileListPanel.add(fileScrollPane, BorderLayout.CENTER);
        fileListPanel.setPreferredSize(new Dimension(200, 100));

        // 파일 경로와 버튼을 담는 패널
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());
        filePathField.setPreferredSize(new Dimension(600, 30));
        filePanel.add(filePathField, BorderLayout.CENTER);

        // 파일 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 10));
        buttonPanel.add(filePanel);
        buttonPanel.add(selectButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exitButton);

        // 텍스트 영역
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setPreferredSize(new Dimension(400, 100));

        // 전체 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(fileListPanel, BorderLayout.WEST);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);


        // 전체 패널을 프레임의 중앙에 배치
        add(mainPanel, BorderLayout.CENTER);

        // 창 닫기 이벤트 처리
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(FileUPload.this, "창을 닫으시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {

                    //소켓 종료
                    try {
                        socketHandler.logout(socketHandler.getId());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    dispose(); // 프레임 닫기
                    System.exit(0); // 프로그램 종료
                }
            }
        });

        setVisible(true);
    }

    //Modal
    //공유할 사용자 선택 모달창
    private void createSendWindow() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("공유할 사용자 선택");

        // 보내기 패널
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new GridLayout(6, 5));

        // 유저 id입력  레이블
        JLabel loginLabel = new JLabel("공유할 사용자 선택");
        loginLabel.setHorizontalAlignment(JLabel.CENTER);

        // id입력 입력 필드
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel idLabel = new JLabel("유저 id:");
        JTextField idTextField = new JTextField(15);
        idPanel.add(idLabel);
        idPanel.add(idTextField);

        // 로그인 버튼
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton sendButton = new JButton("전송");
        buttonPanel.add(sendButton);

        // 컴포넌트를 패널에 추가
        sendPanel.add(loginLabel);
        sendPanel.add(idPanel);
        sendPanel.add(buttonPanel);

        // 전체 프레임에 패널을 중앙에 배치
        frame.add(sendPanel, BorderLayout.CENTER);

        // 전송 이벤트
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetId = idTextField.getText();

                if(targetId.length() == 0){
                    JOptionPane.showMessageDialog(null, "보낼 대상을 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if(selectedFiles.length == 0){
                    JOptionPane.showMessageDialog(null, "파일을 선택하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    return;
                }

                //선택된 파일 서버에 전송
                String id = socketHandler.getId();

                //파일 업로드
                try {
                    socketHandler.shareFile(id, selectedFiles, targetId);

                    //파일 리스트 초기화 및 재렌더링
                    resetSelectedFiles();
                    refreshFileList();

                    frame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        frame.pack(); // 윈도우 크기 자동 조절
        frame.setLocationRelativeTo(null); // 중앙에 위치
        frame.setVisible(true);
    }

    //삭제 모달창
    private void delSendWindow() {
        JFrame sendWindow = new JFrame();
        sendWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        sendWindow.setTitle("파일 삭제");
        sendWindow.setSize(300, 100);
        sendWindow.setLocationRelativeTo(this);

        // 삭제 패널
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new GridLayout(6, 5));

        // 삭제  레이블
        JLabel loginLabel = new JLabel("DelPanel");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loginLabel.setHorizontalAlignment(JLabel.CENTER);

        // 삭제 필드
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel idLabel = new JLabel("삭제할 파일명:");
        JTextField idTextField = new JTextField(15);
        JButton delButton = new JButton("삭제");
        idPanel.add(idLabel);
        idPanel.add(idTextField);
        idPanel.add(delButton);

        // 컴포넌트를 패널에 추가
        sendPanel.add(loginLabel);
        sendPanel.add(idPanel);

        // 삭제 이벤트
        delButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String deleteFileName = idTextField.getText();

                if(deleteFileName.length() == 0){
                    JOptionPane.showMessageDialog(null, "삭제할 파일명을 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    sendWindow.dispose();
                    return;
                }

                //서버 요청
                //선택된 파일 서버에 전송
                String id = socketHandler.getId();

                //파일 업로드
                try {
                    socketHandler.fileDelete(id, deleteFileName);
                    sendWindow.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        // 전체 프레임에 패널을 중앙에 배치
        sendWindow.add(sendPanel, BorderLayout.CENTER);
        sendWindow.pack(); // 윈도우 크기 자동 조절
        sendWindow.setLocationRelativeTo(null); // 중앙에 위치
        sendWindow.setVisible(true);
    }

    //파일 선택 메서드
    private void uploadFile(String id, File file) {
        File[] updatedFiles = new File[selectedFiles.length + 1];
        System.arraycopy(selectedFiles, 0, updatedFiles, 0, selectedFiles.length);
        updatedFiles[selectedFiles.length] = file;
        selectedFiles = updatedFiles;
    }

    private void refreshFileList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (File file : selectedFiles) {
            listModel.addElement(file.getName());
        }
        fileList.setModel(listModel);
        fileList.setSelectedIndex(-1);
        filePathField.setText("");
    }

    private void resetSelectedFiles() {
        selectedFiles = new File[0];
    }

    /**
     * 메시지를 수신받아 textArea에 추가하는 데몬 쓰레드
     */
    private void receiveMessage() {
        Thread thread = new Thread(new Runnable() {
            int count = 0;

            @Override
            public void run() {
                while (socketHandler.getSocket().isConnected()) { // 연결 상태 확인
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    List<String> msgArr = socketHandler.getMsgArr();
                    String last = null;

                    if(!msgArr.isEmpty()){
                        last = msgArr.get(msgArr.size() - 1);
                    }

                    String finalLast = last;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if ( count != msgArr.size() ) {
                                messageArea.append(finalLast + "\n");
                                count = msgArr.size();
                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }
}