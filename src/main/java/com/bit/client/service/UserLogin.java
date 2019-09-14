package com.bit.client.service;

import com.bit.client.dao.AccountDao;
import com.bit.client.entity.User;
import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class UserLogin {
    private JPanel RegPanel;
    private JPanel userNamePanel;
    private JTextField usernameText;
    private JLabel userName;
    private JPanel passwordPanel;
    private JLabel password;
    private JButton regBtn;
    private JButton loginBtn;
    private JPasswordField passwordText;

    private AccountDao accountDao = new AccountDao();

    public UserLogin() {
        JFrame frame = new JFrame("登录页面");
        frame.setContentPane(RegPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(600,400);
        frame.setVisible(true);
        //这是点击注册
        regBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UserReg();
            }
        });
        //这是点击登录
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName  = usernameText.getText();
                String password = String.valueOf(passwordText.getPassword());

                User user = accountDao.userLogin(userName, password);

                if (user != null){
                    JOptionPane.showMessageDialog(null,"登录成功",
                            "提示信息",JOptionPane.INFORMATION_MESSAGE);

                    //1.与服务器建立连接, 将自己的用户名与Socket保存到服务端缓存
                    Connect2Server connect2Server = new Connect2Server();

                    MessageVo client2Server = new MessageVo();
                    client2Server.setType(1);
                    client2Server.setContent(userName);
                    String msgString = CommUtil.object2String(client2Server);
                    try {
                        PrintStream printStream = new PrintStream(connect2Server.getOut(),true,"UTF-8");
                        printStream.println(msgString);


                        Scanner scanner = new Scanner(connect2Server.getIn());
                       if(scanner.hasNextLine()){
                            String msgFromServerStr = scanner.nextLine();
                            MessageVo msgFromServer = (MessageVo) CommUtil.string2Object(msgFromServerStr, MessageVo.class);
                            Set<String> names = (Set<String>) CommUtil.string2Object(msgFromServer.getContent(),Set.class);

                            //现在是关闭 登录页面 然后呢  去往好友列表页面
                            //1.关闭
                           frame.setVisible(false);
                            //2.打开好友页面, 并且传递信息  自己用户名 好友列表 和 我这个连接的Socket
                           new FriendLIst(userName,names,connect2Server);
                           // new FriendLIst();
                            }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    JOptionPane.showMessageDialog(null,"登录失败",
                            "提示信息",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        new UserLogin();
    }
}
