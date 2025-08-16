package com.checkmate.bub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MainController {

    @GetMapping("/")
    public RedirectView main() {
        return new RedirectView("/main.html");  // /static/main.html로 리다이렉트 (정적 파일 서빙)
    }
}
