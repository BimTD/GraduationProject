package org.example.graduationproject.controllers.user;


import org.example.graduationproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/about")
public class AboutController extends BaseController {

    @GetMapping
    public String aboutPage() {
        return "user/about";
    }
}
