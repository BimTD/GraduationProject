package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/contact")
public class ContactController extends BaseController {

    @GetMapping
    public String contactPage() {
        return "user/contact";
    }

    @PostMapping
    public String handleContactForm(@RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String subject,
                                   @RequestParam String message,
                                   Model model) {
        // TODO: Implement email sending logic here
        // For now, just add a success message
        model.addAttribute("successMessage", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể.");
        System.out.println("Contact Form Submission:");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        
        return "user/contact";
    }
}