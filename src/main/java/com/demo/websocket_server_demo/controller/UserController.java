package com.demo.websocket_server_demo.controller;

import com.demo.websocket_server_demo.entity.User;
import com.demo.websocket_server_demo.webSocket.MainServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin
@RequestMapping("user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap();

    @GetMapping("onlinecount")
    public int getOnLineCount() {
        return MainServer.number;
    }

    @PostMapping("signup")
    public int signup(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        user.setFriends(new LinkedList<>());
        userMap.put(user.getUserId(), user);
        return login(user, request, response);
    }

    @PostMapping("login")
    public int login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        if (null != userMap.get(user.getUserId())) {
            request.getSession().setAttribute("userId", userMap.get(user.getUserId()));
            Cookie uId = new Cookie("userId", user.getUserId());
            uId.setPath("/");
            Cookie uName = new Cookie("userName", userMap.get(user.getUserId()).getUserName());
            uName.setPath("/");
            response.addCookie(uId);
            response.addCookie(uName);
            return 1;
        }
        return -1;
    }

    @PostMapping("logout")
    public void logout(@RequestBody String userId, HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("userId");
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    @PostMapping("addFriend")
    public int addFriend(@RequestParam String userId, @RequestParam String friendId) {
        User friend = userMap.get(friendId);
        if (null == friend) {
            return -1;
        }
        userMap.get(friendId).getFriends().add(friend);
        return 1;
    }

    @PostMapping("delFriend")
    public int delFriend(@RequestParam String userId, @RequestParam String friendId) {
        userMap.get(userId).getFriends().remove(userMap.get(friendId));
        return 1;
    }

    @GetMapping("getFriends")
    public List<User> getFriends(@RequestParam String userId) {
        User user = userMap.get(userId);
        if (user != null) {
            return user.getFriends();
        }
        return null;
    }
}