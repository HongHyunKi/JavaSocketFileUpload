package client.gui;

import client.FileStorage;
import client.SocketHandler;

import java.awt.*;
import javax.swing.*;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class Login {
    Socket socket;
    SocketHandler socketHandler;

    public Login(SocketHandler socketHandler){
        this.socketHandler = socketHandler;

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("로그인");

        // 로그인 패널
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(6, 5));

        // 로그인 레이블
        JLabel loginLabel = new JLabel("LOGIN");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loginLabel.setHorizontalAlignment(JLabel.CENTER);

        // 아이디 입력 필드
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel idLabel = new JLabel("사용자 이름:");
        JTextField idTextField = new JTextField(15);
        idPanel.add(idLabel);
        idPanel.add(idTextField);

        // 로그인 버튼
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginButton = new JButton("로그인");
        buttonPanel.add(loginButton);

        // 컴포넌트를 로그인 패널에 추가
        loginPanel.add(loginLabel);
        loginPanel.add(idPanel);
        loginPanel.add(buttonPanel);

        // 전체 프레임에 로그인 패널을 중앙에 배치
        frame.add(loginPanel, BorderLayout.CENTER);

        frame.pack(); // 윈도우 크기 자동 조절
        frame.setLocationRelativeTo(null); // 중앙에 위치
        frame.setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 로그인 버튼 클릭 시 실행되는 로직
                String id = idTextField.getText();

                // 요청 전송
                try {
                    socketHandler.login(id);

                    // 파일 저장소 생성
                    String storagePath = "src/client//Storage/" + id; // 저장소 디렉토리 경로
                    FileStorage fileStorage = new FileStorage(storagePath);
                    fileStorage.initialize();

                    // 다른 페이지로 이동하는 메시지를 표시
                    JOptionPane.showMessageDialog(frame, ""+id+"님 환영합니다.", "알림", JOptionPane.INFORMATION_MESSAGE);

                    // 다른 페이지로 이동
                    FileUPload fileUploadPage = new FileUPload(socketHandler, fileStorage); //소켓
                    frame.dispose(); // 현재 페이지 닫기

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}