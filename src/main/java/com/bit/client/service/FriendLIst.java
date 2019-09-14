package com.bit.client.service;

import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendLIst {
    private JPanel firendPanel;
    private JScrollPane friendListPanel;
    private JButton createGroupBtn;
    private JScrollPane groupListPanel;

    private JFrame frame;

    private String myName;
    private Set<String> users;
    private Connect2Server connect2Server;
    //存储群名称以及群好友
    private Map<String,Set<String>> groupList = new ConcurrentHashMap<>();
    //缓存所有私聊界面
    private Map<String, privateChatGUI> privateChatGUIMap = new ConcurrentHashMap<>();

    //缓存所有群聊界面
    private Map<String, GroupChatGUI> groupChatGUIMap = new ConcurrentHashMap<>();

//axis 就是轴   y_axis 就是y轴  x_axis 就是x轴 不过都是大写


//后台线程一直接受服务器的
    private class DaemonTask implements Runnable{
        private Scanner in = new Scanner(connect2Server.getIn());

        private DaemonTask() throws IOException {
        }

        @Override
        public void run() {
            while (true){
                if (in.hasNextLine()){
                    String msgStr = in.nextLine();
                    //判断是否为Json 字符串
                    if (msgStr.startsWith("{")){
                        MessageVo msgFromPrivateChat = (MessageVo) CommUtil.string2Object(msgStr,MessageVo.class);
                         if (msgFromPrivateChat.getType().equals(2)){
                            String content = msgFromPrivateChat.getContent();
                            String friendName = content.split("-")[0];
                            String msg = content.split("-")[1];

                            //判断是否缓存的有界面
                            if (privateChatGUIMap.containsKey(friendName)){
                                privateChatGUI privateChatGUI = privateChatGUIMap.get(friendName);
                                privateChatGUI.getFrame().setVisible(true);
                                privateChatGUI.readFromServer(friendName+"说:"+msg);
                            } else {
                                privateChatGUI privateChatGUI = new privateChatGUI(myName, friendName, connect2Server);
                                privateChatGUI.readFromServer(friendName+"说:"+msg);
                                privateChatGUIMap.put(friendName,privateChatGUI);
                            }
                        } else if (msgFromPrivateChat.getType().equals(4)){
                             /*
                              * type = 4
                              * content = senderName-text
                              * to = groupName - friends Set<>
                              * */
                             String content = msgFromPrivateChat.getContent();
                             String senderName = content.split("-")[0];
                             String msgFromServer = "";
                             if (content.split("-").length > 1){
                                  msgFromServer = content.split("-")[1];
                             }
                             String to  = msgFromPrivateChat.getTo();
                             String groupName = to.split("-")[0];

                             //若次群在群聊列表中
                             if (groupList.containsKey(groupName)){
                                 if (groupChatGUIMap.containsKey(groupName)){
                                     GroupChatGUI groupChatGUI = groupChatGUIMap.get(groupName);
                                     groupChatGUI.readFromServer(senderName+"说: "+msgFromServer);

                                 } else {
                               /*     String myName, Set<String> friends,
                                         String groupName, Connect2Server connect2Server*/
                                     GroupChatGUI groupChatGUI = new GroupChatGUI(myName, users, groupName, connect2Server);
                                     groupChatGUI.readFromServer(senderName+"说: "+msgFromServer);
                                     groupChatGUIMap.put(groupName,groupChatGUI);

                                 }
                             } else {
                                 //如果没有在群聊列表中 说明好没有被本对象 GUIgroupMap收集
                                 Set<String> friends = (Set<String>) CommUtil.string2Object(to.split("-")[1],Set.class);
                                 groupList.put(groupName,friends);
                                 loadGroupList();
                                 GroupChatGUI groupChatGUI = new GroupChatGUI(myName, users, groupName, connect2Server);
                                 groupChatGUIMap.put(groupName,groupChatGUI);
                                 groupChatGUI.readFromServer(senderName+"说: "+msgFromServer);
                             }
                         }
                    } else if (msgStr.startsWith("userLogin")){
                        String str = msgStr.split(":")[1];
                        users.add(str);
                        JOptionPane.showMessageDialog(frame,str+"上线了","上线提醒",JOptionPane.INFORMATION_MESSAGE);
                        loadUsers();
                    }
                }
            }
        }
    }




    public FriendLIst(String myName, Set<String> users, Connect2Server connect2Server) throws IOException {
        this.myName = myName;
        this.users = users;
        this.connect2Server = connect2Server;
        frame = new JFrame(myName);
        frame.setContentPane(firendPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(800,600);
        frame.setVisible(true);

    //创建好之后 我就需要好友列表加载了
        loadUsers();
       //要一直接收服务器的提醒消息,所有新建一个后台线程一致执行任务
        DaemonTask daemonTask = new DaemonTask();
        Thread thread = new Thread(daemonTask);
        thread.setDaemon(true);
        thread.start();
        createGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 /* String myName, Set<String> friends,
                        Connect2Server connect2Server,
                        FriendLIst friendLIst*/
                new CreateGroupGUI(myName,users,connect2Server,FriendLIst.this);
            }
        });
    }

    private void loadUsers() {
        JPanel friends = new JPanel();
        JLabel[] userLabels = new JLabel[users.size()];
        friends.setLayout(new BoxLayout(friends,BoxLayout.Y_AXIS));

        int i = 0;
        Iterator<String> userIterator = users.iterator();
        while (userIterator.hasNext()){
            String name = userIterator.next();
            userLabels[i] = new JLabel(name);
            //添加点击事件
            userLabels[i].addMouseListener(new PrivateLabelAction(name));
            friends.add(userLabels[i]);
            i++;
        }
        friendListPanel.setViewportView(friends);
        //设置滚动条垂直
        friendListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //重新布局
        friends.revalidate();
        friendListPanel.revalidate();
    }

    private class PrivateLabelAction implements MouseListener {
        String labelName;
        //鼠标点击事件执行
        @Override
        public void mouseClicked(MouseEvent e) {
            //判断好友列表私聊界面缓存是否已经有指定标签
            if (privateChatGUIMap.containsKey(labelName)){
                privateChatGUI pGUI = privateChatGUIMap.get(labelName);
                pGUI.getFrame().setVisible(true);
            } else {
                // 第一次点击，创建私聊界面
                privateChatGUI priGUI = new privateChatGUI(
                        myName,labelName,connect2Server
                );
                privateChatGUIMap.put(labelName,priGUI);            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        public PrivateLabelAction(String labelName) {
            this.labelName = labelName;
        }
    }

    private class GroupLabelAction implements MouseListener{

        private String groupName;
        public GroupLabelAction(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (groupChatGUIMap.containsKey(groupName)){
                GroupChatGUI groupChatGUI = groupChatGUIMap.get(groupName);
                groupChatGUI.getFrame().setVisible(true);
            } else {
         /*       String myName, Set<String> friends,
                        String groupName, Connect2Server connect2Server*/
                GroupChatGUI groupChatGUI = new GroupChatGUI(myName, users, groupName, connect2Server);
                groupChatGUIMap.put(groupName,groupChatGUI);
            }
        }


        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
    public void loadGroupList(){
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));

        JLabel[] jLabels =  new JLabel[groupList.size()];

        Set<String> groupNames = groupList.keySet();
        Iterator<String> iterator = groupNames.iterator();
        //取得每个群名 加入groupListPanel
        int i= 0;
        while (iterator.hasNext()){
            String next = iterator.next();
            System.out.println(next);
            jLabels[i] = new JLabel(next);
            //设置JLable点击事件
            jLabels[i].addMouseListener(new GroupLabelAction(next));
            jPanel.add(jLabels[i]);
            i++;
        }
        groupListPanel.setViewportView(jPanel);
        //滚动..了慢慢慢慢 应该是吧
        groupListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        groupListPanel.revalidate();
    }

    public void addGroup(String groupeName,Set<String> users){
        this.groupList.put(groupeName,users);
    }
}
