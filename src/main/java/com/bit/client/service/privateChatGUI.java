package com.bit.client.service;

import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;

public class privateChatGUI {
    private JPanel privateChatPanel;
    private JTextArea readFromServer;
    private JTextField msg2Server;

    public JFrame getFrame() {
        return frame;
    }

    private JFrame frame;
    private String myName;
    private String friendName;
    private Connect2Server connect2Server;
    private PrintStream out;


    //首先得到 我自己的名字 好友的名字 以及 socket
    public privateChatGUI(String myName, String friendName,
                          Connect2Server connect2Server) {
        this.myName = myName;
        this.friendName = friendName;
        this.connect2Server = connect2Server;
        try {
            this.out = new PrintStream(connect2Server.getOut(),true,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame = new JFrame("与"+friendName+"私聊中...");
        frame.setContentPane(privateChatPanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(400,400);
        frame.setVisible(true);
        //msg2Server 这就是要发往服务器的对话框
        msg2Server.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //现在在对话框里
                StringBuilder sb = new StringBuilder();
                sb.append(msg2Server.getText());
                String msg = sb.toString();
                //捕捉 按下Enter键
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    MessageVo msg2Server1 = new MessageVo();
                    msg2Server1.setType(2);
                    msg2Server1.setContent(myName+"-"+msg);
                    msg2Server1.setTo(friendName);
                    String str2Server = CommUtil.object2String(msg2Server1);
                    out.println(str2Server);

                    //然后把自己的话,也输出到read2Server;
                    readFromServer(myName+"说: "+msg);
                    msg2Server.setText("");
                }
            }
        });
    }

   public void readFromServer(String myStr) {
        readFromServer.append(myStr+"\n");
    }
}
