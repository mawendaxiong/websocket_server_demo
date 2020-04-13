package com.demo.websocket_server_demo.webSocket;

import com.alibaba.fastjson.JSONObject;
import com.demo.websocket_server_demo.entity.OffLineMsg;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@ServerEndpoint("/websocketTest/{userId}")
public class MainServer {
    /**
     * OnOpen OnClose这两个注解下的方法是不能带参数，
     * 但是如果加了参数是不会报错的，但是项目跑起来会报错
     * A parameter of type [class java.lang.String] was found on method[onOpen] of class
     * [java.lang.reflect.Method] that did not have a @PathParam annotation
     * 所以这两个注解的方法要加参数需要加上@PathParam来修饰参数
     * OnMessage只能有一个message参数，需要其他额外的参数也需要@PathParam来修饰参数
     */
    private Session session;

    private String userId;

    /**
     * 登陆的用户会往map添加
     * 退出的用户会从map移除
     */
    private static ConcurrentHashMap<String, MainServer> onLineUserMap = new ConcurrentHashMap<>();

    /**
     * 用于存放离线消息
     * 键是接收人的userId
     * 值是OffLineMsg对象
     * OffLineMsg->fromUserId 发送消息的用户
     * OffLineMsg->LinkedList<String> 消息集合
     * 当用户连接socket的时候
     */
    private static ConcurrentHashMap<String, LinkedList<OffLineMsg>> offLineMsg = new ConcurrentHashMap<>();

    public static int number;

    private static ScheduledExecutorService executors = Executors.newScheduledThreadPool(2);

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (null == onLineUserMap.get(userId)) {
            this.session = session;
            this.userId = userId;
            onLineUserMap.put(userId, this);
            numberAdd();
            System.out.println("new user is coming! person number is :" + number);
            LinkedList<OffLineMsg> offLineMsgs = offLineMsg.get(userId);
            if (null != offLineMsgs && offLineMsgs.size() > 0) {
                try {
                    this.session.getBasicRemote().sendText(JSONObject.toJSONString(offLineMsgs));
                    while (true) {
                        offLineMsgs.removeFirst();
                        if (offLineMsgs.isEmpty()) break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        onLineUserMap.remove(userId);
        numberDec();
        System.out.println("a user is leaving! person number is :" + number);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String toUserId) {
        try {

            sendMessage(message, toUserId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String toUserId) throws IOException {
        MainServer mainServer = onLineUserMap.get(toUserId);
        if (mainServer != null) {
            mainServer.session.getBasicRemote().sendText(message);
        } else {
            LinkedList<String> msgList;
            if (null == offLineMsg.get(toUserId)) {
                LinkedList<OffLineMsg> msgs = new LinkedList<>();
                OffLineMsg msg = new OffLineMsg();
                msg.setFromUserId(this.userId);
                LinkedList<String> userMsg = new LinkedList<>();
                userMsg.addLast(message);
                msg.setMsg(userMsg);
                msgs.addLast(msg);
                offLineMsg.put(toUserId, msgs);
                return;
            }
            offLineMsg.get(toUserId).parallelStream().forEach(msg -> {
                if (msg.getFromUserId().equals(this.userId)) {

                }
            });
        }
    }

    private synchronized void numberAdd() {
        number++;
    }

    private synchronized void numberDec() {
        if (0<number){
            number--;
        }
    }
}
