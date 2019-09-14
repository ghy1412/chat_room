package com.bit.client.dao;



import com.bit.client.entity.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AccountDao extends BasedDao {
    //根据返回值判定是否成功
    public boolean userReg(User user){
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            String sql = "insert user(username,password,brief) values(?,?,?)";
            preparedStatement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,user.getUserName());
            preparedStatement.setString(2, DigestUtils.md5Hex(user.getPassword()));
            preparedStatement.setString(3,user.getBrief());
            int row = preparedStatement.executeUpdate();

            if (row == 1){
                return true;
            }
        } catch (Exception e) {
            System.err.println("登陆失败!!!");
            e.printStackTrace();
        } finally {
            closeResources(connection,preparedStatement);
        }
        return false;
    }

    public User userLogin(String username, String password){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            String sql = "select * from  user where username = ? and password = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,DigestUtils.md5Hex(password));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setBrief(resultSet.getString("brief"));
                return user;
            }
        } catch (Exception e) {
            System.err.println("用户登录失败!!!");
            e.printStackTrace();
        } finally {
            closeResources(connection,preparedStatement,resultSet);
        }
        return null;
    }
}
