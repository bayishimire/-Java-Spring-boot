package com.hospital.advice;

import com.hospital.model.Patient;
import com.hospital.model.User;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class GlobalAdvice {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public GlobalAdvice(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @ModelAttribute("currentUser")
    public User globalCurrentUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        // Prevent recursive errors on the error page itself
        System.err.println("Global Error Caught: " + e.getMessage());
        return "error";
    }
}
