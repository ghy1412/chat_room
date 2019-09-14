package com.bit.client.service;

import com.bit.client.dao.AccountDao;
import com.bit.client.entity.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserReg {
    private JTextField userNameText;
    private JTextField briefText;
    private JPanel RegPanel;
    private JPasswordField passwordText;
    private JButton confirmBtn;

    private AccountDao accountDao = new AccountDao();
    //这是构造方法
    public UserReg() {
        JFrame frame = new JFrame("注册页面");
        frame.setContentPane(RegPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        //点击提交按钮触发此方法
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //1.获取界面上三个控件的内容
                String userName = userNameText.getText();
                String password = String.valueOf(passwordText.getPassword());
                String brief = briefText.getText();
                //2.调用Dao层的方法将信息持久化到数据库
                User user = new User();
                user.setUserName(userName);
                user.setPassword(password);
                user.setBrief(brief);
                if (accountDao.userReg(user)){
                    //提示注册成功
                    //跳转到登录页面
                    JOptionPane.showMessageDialog(null,"注册成功",
                            "提示信息",JOptionPane.INFORMATION_MESSAGE);
                    frame.setVisible(false);
                } else {
                    //弹出提示框告知用户注册失败
                    //保留当前注册页面
                    JOptionPane.showMessageDialog(null,"注册失败",
                            "提示信息",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

}
