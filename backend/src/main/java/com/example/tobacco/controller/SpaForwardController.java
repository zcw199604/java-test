package com.example.tobacco.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/login",
            "/dashboard",
            "/profile",
            "/403",
            "/purchase/{path:[^.]*}",
            "/purchase/{path:[^.]*}/{subpath:[^.]*}",
            "/sale/{path:[^.]*}",
            "/sale/{path:[^.]*}/{subpath:[^.]*}",
            "/sale/{path:[^.]*}/{subpath:[^.]*}/{leaf:[^.]*}",
            "/inventory/{path:[^.]*}",
            "/inventory/{path:[^.]*}/{subpath:[^.]*}",
            "/admin/{path:[^.]*}",
            "/admin/{path:[^.]*}/{subpath:[^.]*}",
            "/report/{path:[^.]*}",
            "/report/{path:[^.]*}/{subpath:[^.]*}",
            "/trace/{path:[^.]*}",
            "/trace/{path:[^.]*}/{subpath:[^.]*}",
            "/audit/{path:[^.]*}",
            "/audit/{path:[^.]*}/{subpath:[^.]*}"
    })
    public String index() {
        return "forward:/index.html";
    }
}
