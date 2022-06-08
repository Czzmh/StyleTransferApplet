package com.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Shen RuiJin
 */
@Controller
public class PathController {

    @GetMapping("/")
    public String welcome(){
        return "login";
    }

    @GetMapping("/index")
    public String toIndex(){
        return "redirect:/psg/all";
    }
}

