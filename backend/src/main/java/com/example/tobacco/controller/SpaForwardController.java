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
            "/sales",
            "/purchase",
            "/purchase/order",
            "/purchase/order/create",
            "/purchase/order/{id:[^.]*}/edit",
            "/purchase/order/{id:[^.]*}/detail",
            "/purchase/inbound",
            "/purchase/analysis",
            "/sale",
            "/sale/order",
            "/sale/order/create",
            "/sale/order/{id:[^.]*}/edit",
            "/sale/order/{id:[^.]*}/detail",
            "/sale/outbound",
            "/sale/performance",
            "/inventory",
            "/inventory/list",
            "/inventory/flow",
            "/inventory/check",
            "/inventory/ledger",
            "/admin",
            "/admin/account",
            "/admin/role",
            "/admin/base-data",
            "/catalog/products",
            "/supplier/list",
            "/admin/log",
            "/admin/config",
            "/report/center",
            "/report/dashboard",
            "/trace/query",
            "/audit/exception"
    })
    public String index() {
        return "forward:/index.html";
    }
}
