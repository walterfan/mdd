package com.github.walterfan.potato.web;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * Created by yafan on 4/5/2018.
 */
@Controller
public class PotatoWebController {


    @RequestMapping(path = {"/potatoes"})
    public String potatoes(Model model) {

        return "potatoes";
    }


    @RequestMapping(path = {"/tomatoes"})
    public String tomatoes(Model model) {
        return "admin/tomatoes";
    }

    @RequestMapping(path = {"/bookmarks"})
    public String bookmarks(Model model) {
        return "admin/bookmarks";
    }

    @RequestMapping(path = {"/articles"})
    public String articles(Model model) {
        return "admin/articles";
    }

    @RequestMapping(path = {"/admin/home"})
    public String admin(Model model) {
        return "admin/home";
    }


}
