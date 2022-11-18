package org.home.productivity.traverse.controller.spec;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class HomeController {

    @GetMapping
    public String hello() {
        log.warn(
                "Welcome home, the root end point '/' will need to be replaced eventually by a frontend. For now we return the index.html static page to gain access to points of interest");
        return "index.html";
    }

}
