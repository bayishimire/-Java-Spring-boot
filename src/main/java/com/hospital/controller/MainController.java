package com.hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) return "redirect:/admin/dashboard";
        if (request.isUserInRole("DOCTOR")) return "redirect:/doctor/dashboard";
        if (request.isUserInRole("RECEPTIONIST")) return "redirect:/receptionist/dashboard";
        if (request.isUserInRole("NURSE")) return "redirect:/nurse/dashboard";
        if (request.isUserInRole("LAB_TECHNICIAN")) return "redirect:/lab/dashboard";
        if (request.isUserInRole("PHARMACIST")) return "redirect:/pharmacist/dashboard";
        if (request.isUserInRole("BILLING_STAFF")) return "redirect:/billing/dashboard";
        if (request.isUserInRole("PATIENT")) return "redirect:/patient/dashboard";
        return "redirect:/";
    }
}
