package com.bit.client.service;

import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;
import sun.security.krb5.internal.ccache.CCacheOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupGUI {
    private JPanel CreateGroupPanel;
    private JPanel friendsListPanel;
    private JTextField groupNameText;
    private JButton submitBtn;
    JFrame frame;
    private PrintStream out;
    private String myName;
    private Connect2Server connect2Server;
    private Set<String> friends;
    private FriendLIst friendLIst;


    public CreateGroupGUI(){
        JFrame frame = new JFrame("CreateGroupGUI");
        frame.setContentPane(CreateGroupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public CreateGroupGUI(String myName, Set<String> friends,
                          Connect2Server connect2Server,
                          FriendLIst friendLIst) {
        frame = new JFrame("创建群聊");
        frame.setContentPane(CreateGroupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        //居中显示
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        try {
            this.connect2Server = connect2Server;
            this.myName = myName;
            this.friends = friends;
            this.friendLIst = friendLIst;
            this.out = new PrintStream(connect2Server.getOut(),true,"UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }


        //想要加载列表盒子 我得知道 好友列表的名字 最后也得把自己加上 和自己的connection
        //加载好友
        friendsListPanel.setLayout(new BoxLayout(friendsListPanel,BoxLayout.Y_AXIS));
        Iterator<String> iterator = friends.iterator();
        while (iterator.hasNext()){
            String  friend = iterator.next();
            JCheckBox jCheckBox = new JCheckBox(friend);
            friendsListPanel.add(jCheckBox);
        }
        //点击按键提交到服务端
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<String> checkBoxFriendList = new HashSet<>();
                Component[] components = friendsListPanel.getComponents();
                for (Component comp : components){
                   JCheckBox jCheckBox =  (JCheckBox)comp;
                   if (jCheckBox.isSelected()){
                       checkBoxFriendList.add(jCheckBox.getText());
                   }
                }
                checkBoxFriendList.add(myName);
                /*
                * 发送给服务器 群名 加  好友名单
                * type: 3
                * connect:groupName
                * to: 好友集合吧[]Set<>
                * */
                //群名啊
                String groupName = groupNameText.getText();
                MessageVo messageVo2Server = new MessageVo();
                messageVo2Server.setType(3);
                messageVo2Server.setContent(groupName);
                messageVo2Server.setTo(CommUtil.object2String(checkBoxFriendList));
                //发送信息 // 然后写服务器接收/////
                out.println(CommUtil.object2String(messageVo2Server));

                //并隐藏此界面
               frame.setVisible(false);
                //增加群
                //刷新群聊好友界面
                friendLIst.addGroup(groupName,checkBoxFriendList);
                friendLIst.loadGroupList();
            }
        });
    }


}
