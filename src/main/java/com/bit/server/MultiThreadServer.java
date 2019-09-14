package com.bit.server;

import com.bit.client.entity.User;
import com.bit.utils.CommUtil;
import com.bit.vo.MessageVo;
import sun.applet.resources.MsgAppletViewer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer {
    //创建服务器 需要具体的端口号但凡配置文件都需要properties文件
    //卧槽  你是傻逼吧  服务器本身就知道自己的IP地址 创建套接字 只需要让你指定端口号
    private static final int PORT;
    //private static final String IP;
    //还有一件事, 我服务器要给你的其他好友发送你上线了之类的东西 让你在他们眼中存活
    //我服务器是不是得知道 你的名字 和你的套接字?????为了对应上 现在我们创建一个Map集合来管理
   private static Map<String, Socket> clients = new ConcurrentHashMap<>();

   //这是保存群聊信息的
    private static  Map<String, Set<String>> groups = new ConcurrentHashMap<>();

    static {
        Properties properties = CommUtil.loadProperties("client.properties");
         PORT = Integer.parseInt(properties.getProperty("PORT"));
      //   IP = properties.getProperty("IP");
    }

    //一个处理具体客户端业务的类
    public static class ExecuteClient implements Runnable{
        //我要处理具体的客户端 我是不是得知道一个具体的客户端  ...是的
        private Socket client;
        private PrintStream out;
        private Scanner in;
        public ExecuteClient(Socket client) throws IOException {
            this.client = client;
            this.out = new PrintStream(client.getOutputStream(),true,"UTF-8");
            this.in = new Scanner(client.getInputStream());
        }

        @Override
        public void run() {
            while (true) {
                //输入
                while (in.hasNextLine()){
                    String msgStr = in.nextLine();
                    MessageVo msgFromClient = (MessageVo) CommUtil.string2Object(msgStr,MessageVo.class);
                    if (msgFromClient.getType().equals(1)){
                        //1.将好友列表信息发送给客户端
                        MessageVo msg2Client =  new MessageVo();
                        msg2Client.setType(1);
                        Set<String> names = clients.keySet();
                        msg2Client.setContent(CommUtil.object2String(names));
                        out.println(CommUtil.object2String(msg2Client));

                        //2.服务端给所有好友列表发送消息,告知尊贵的您上线哩
                        String userName = msgFromClient.getContent();
                        sendUsersLogin("userLogin:"+userName);
                        //3.缓存自己信息, 到clients集合
                        clients.put(userName,client);
                        System.out.println(userName+"上线了!");
                        System.out.println("当前聊天室共有"+
                                clients.size()+"人");
                    } else if (msgFromClient.getType().equals(2)){
                        //取得朋友的socket  输出流
                        String content = msgFromClient.getContent();
                        String myName = content.split("-")[0];
                        String friendName = msgFromClient.getTo();
                        MessageVo msg2Client = new MessageVo();
                        msg2Client.setType(2);
                        msg2Client.setContent(myName+"-"+content.split("-")[1]);

                        try {
                            PrintStream printStream = new PrintStream(clients.get(friendName).getOutputStream());
                            printStream.println(CommUtil.object2String(msg2Client));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (msgFromClient.getType().equals(3)){
                        //创建群聊 //只用把我创建的群聊信息保存在服务器就好
                        /*
                         * 发送给服务器 群名 加  好友名单
                         * type: 3
                         * connect:groupName
                         * to: 好友集合吧[]Set<>
                         * */
                        String groupName = msgFromClient.getContent();
                        Set<String> checkedFriends = (Set<String>) CommUtil.string2Object(msgFromClient.getTo(),Set.class);
                        groups.put(groupName, checkedFriends);
                    } else if (msgFromClient.getType().equals(4)){
                        /*
                         * type = 4
                         * content = senderName-text
                         * to = groupName - friends Set<>
                         * */
                        Set<String> users = groups.get(msgFromClient.getTo());
                        Iterator<String> iterator = users.iterator();

                        MessageVo send2GroupClients= new MessageVo();
                        send2GroupClients.setType(4);
                        send2GroupClients.setContent(msgFromClient.getContent());
                        send2GroupClients.setTo(msgFromClient.getTo()+"-"+CommUtil.object2String(users));

                        while (iterator.hasNext()){
                            String name = iterator.next();
                            Socket client = clients.get(name);
                            try {
                                PrintStream printStream =
                                        new PrintStream(client.getOutputStream(),true,"UTF-8");
                                printStream.println(CommUtil.object2String(send2GroupClients));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        private void sendUsersLogin(String msg){
            for (Map.Entry<String,Socket> usersEntry: clients.entrySet()){
                Socket user = usersEntry.getValue();
                PrintStream printStream = null;
                try {
                    printStream = new PrintStream(user.getOutputStream(),true,"UTF-8");
                    printStream.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //服务器得启动 得需要一个主方法吧
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        //现在创建一个线程池, 来创建服务线程 为每个客户端提供隔离但相同的服务
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        //老办法 .阻塞等待客户端喽  阻塞方法 : accept()

        for (int i = 0; i < 50; i++) {
            System.out.println("等待客户端连接...");
            Socket client = serverSocket.accept();
            System.out.println(client.getPort());
            executorService.submit(new ExecuteClient(client));
        }

    }
}
