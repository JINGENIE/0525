package com.kbstar.controller;

import com.kbstar.dto.User;
import com.kbstar.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class MainController {

    @Value("${adminserver}")
    String adminserver;

    @Autowired
    UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @RequestMapping("/")
    public String main(Model model) {
        model.addAttribute("adminserver", adminserver);
        return "index";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("center", "login");
        return "index";
    }

    @RequestMapping("/loginimpl")
    public String loginimpl(Model model, String user_id, String user_pwd, HttpSession session) throws Exception {
        User user = null;
        String nextPage = "loginfail";

        try {
            user = userService.get(user_id);
            logger.info(user_id);

            if (user != null && user_pwd.equals(user.getUser_pwd())) {
                nextPage = "/shop/center";
                session.setMaxInactiveInterval(10000);
                session.setAttribute("loginuser", user);
            } else {
                model.addAttribute("center", nextPage);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            model.addAttribute("center", nextPage);
        }
        return "index";
    }


    @RequestMapping("/logout")
    public String logout(Model model, HttpSession session) {
        if(session!=null){
            session.invalidate();
        }
        return "index";
    }

    @RequestMapping("/register")
    public String register(Model model) {
        model.addAttribute("center", "register");
        return "index";
    }

    // id 중복체크
    @RequestMapping(value="/checkId", method= RequestMethod.POST)
    @ResponseBody // RestController가 아닌 일반 Controller이기에 ResponseBody 필수
    public String checkId(String user_id){
        User user;
        try {
            user = userService.get(user_id);
        } catch (Exception e) {
            throw new RuntimeException("fail");
        }
        // user가 있다면, 즉, id 중복이면
        if(user != null){
            return "fail";
        }
        return "success";
    }

    // 회원 가입
    @RequestMapping(value="/registerimpl", method= RequestMethod.POST)
    public String registerimpl(Model model, User user, HttpSession session){
        try {
            user.setUser_pwd(encoder.encode(user.getUser_pwd())); // 비밀번호 암호화
            userService.register(user);
            session.setAttribute("loginadm", user);
            logger.info("회원가입 성공");
        } catch (Exception e) {
            throw new RuntimeException("회원가입 실패");
        }

        return "redirect:/";
    }

}
