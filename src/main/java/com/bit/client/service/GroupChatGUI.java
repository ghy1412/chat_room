package com.bit.client.service;

import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

public class GroupChatGUI {
    private JTextArea readFromServer;
    private JTextField send2Server;
    private JPanel GroupChatGUI;
    private JPanel firendListPanel;
    private JFrame frame;
    private String myName;
    private Set<String> friends;
    private String groupName;
    private Connect2Server connect2Server;


    public GroupChatGUI(String myName, Set<String> friends,
                        String groupName, Connect2Server connect2Server) {
        this.myName = myName;
        this.friends = friends;
        this.groupName = groupName;
        this.connect2Server =connect2Server;
        frame = new JFrame(groupName);
        frame.setContentPane(GroupChatGUI);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
         //1.加载好友列表
        firendListPanel.setLayout(new BoxLayout(firendListPanel,BoxLayout.Y_AXIS));
        Iterator<String> iterator = friends.iterator();
        while (iterator.hasNext()){
            String name = iterator.next();
            JLabel jLabel = new JLabel(name);
            //其实也可以加一个 adMouse事件
            firendListPanel.add(jLabel);
        }

        send2Server.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(send2Server.getText());

                String str2Server = send2Server.getText();
                //现在如果接收到空格就发送
                //event 事件
                /*
                 * type = 4
                 * content = senderName-text
                 * to = groupName
                 * */
                send2Server.setText("");
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    MessageVo msg2Server = new MessageVo();
                    msg2Server.setType(4);
                    msg2Server.setContent(myName+"-"+str2Server);
                    msg2Server.setTo(groupName);
                    try {
                        PrintStream printStream = new PrintStream(connect2Server.getOut());
                        printStream.println(CommUtil.object2String(msg2Server));
                        //现在该该该该该写服务器了
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
    public void readFromServer(String msg){
     readFromServer.append(msg+"\n");
    }
    public JFrame getFrame(){
        return frame;
    }
}
