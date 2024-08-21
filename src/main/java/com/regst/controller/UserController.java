package com.regst.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.regst.entity.User;
import com.regst.service.EmailService;
import com.regst.service.UserService;


@Controller
public class UserController {
	 @Autowired
	    private UserService userService;
	
	 @Autowired
	    private EmailService emailService;
	
	@GetMapping("/login")
    public String loginPage() {
        return "login";
    }
	
	@PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isAccountLocked()) {
                model.addAttribute("error", "Your Account Is Locked");
                return "login";
            } else if (user.getPassword().equals(password)) {
                return "redirect:/welcome";
            } else {
                model.addAttribute("error", "Invalid Credentials");
                return "login";
            }
        } else {
            model.addAttribute("error", "Invalid Credentials");
            return "login";
        }
    }
	@GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
	
	 @PostMapping("/register")
	    public String register(@ModelAttribute User user, Model model) {
	        user.setPassword(UUID.randomUUID().toString().substring(0, 8)); // Generate random password
	        user.setAccountLocked(true); // Lock the account initially
	        userService.saveUser(user);

	        // Create the unlock link
	        String unlockUrl = "http://localhost:8080/unlock-account?email=" + user.getEmail();

	        // Send email to user with unlock link and temporary password
	        emailService.sendSimpleMessage(user.getEmail(), 
	            "Unlock your account", 
	            "Click the link to unlock your account: " + unlockUrl + 
	            "\nYour temporary password is: " + user.getPassword());

	        model.addAttribute("message", "Please check your email to unlock account");
	        return "register";
	    }

	    @GetMapping("/unlock-account")
	    public String unlockAccountPage() {
	        return "unlock";
	    }

	    @PostMapping("/unlock-account")
	    public String unlockAccount(@RequestParam String email, @RequestParam String tempPassword, @RequestParam String newPassword, Model model) {
	        Optional<User> userOpt = userService.findByEmail(email);
	        
	        if (userOpt.isPresent()) {
	            User user = userOpt.get();
	            if (user.getPassword().equals(tempPassword)) {
	                user.setPassword(newPassword);
	                user.setAccountLocked(false);
	                userService.saveUser(user);
	                model.addAttribute("message", "Account unlocked, please proceed with login.");
	                return "login";
	            } else {
	                model.addAttribute("error", "Invalid Temporary Password");
	                return "unlock";
	            }
	        } else {
	            model.addAttribute("error", "Invalid Email");
	            return "unlock";
	        }
	    }

	    @GetMapping("/forgot-password")
	    public String forgotPasswordPage() {
	        return "forgot-password";
	    }

	    @PostMapping("/forgot-password")
	    public String forgotPassword(@RequestParam String email, Model model) {
	        Optional<User> userOpt = userService.findByEmail(email);
	        if (userOpt.isPresent()) {
	            // Send email to user with password
	            emailService.sendSimpleMessage(userOpt.get().getEmail(), 
	                "Your Password", 
	                "Your password is: " + userOpt.get().getPassword());
	            model.addAttribute("message", "Password sent to your email.");
	            return "forgot-password";
	        } else {
	            model.addAttribute("error", "Email not registered.");
	            return "forgot-password";
	        }
	    }

}
