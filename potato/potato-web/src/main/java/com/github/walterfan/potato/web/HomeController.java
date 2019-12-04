package com.github.walterfan.potato.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * @Author: Walter Fan
 **/
@Controller
public class HomeController {
    @Value("${potato.default.userName:Walter}")
    private String defaultUserName;
    @Value("${potato.default.userId:53a3093e-6436-4663-9125-ac93d2af91f9}")
    private String defaultUserId;

    @RequestMapping(path = {"/"})
    public String welcome(Model model) {
        model.addAttribute("message", defaultUserName + ",  welcome to potato workshop at " + new Date());
        return "welcome";
    }

    @RequestMapping(path = {"/error"})
    public String error( Model model) {
        model.addAttribute("message", defaultUserName + ",  There is an error of your request, please retry or contact admin");
        return "errors";
    }


}

