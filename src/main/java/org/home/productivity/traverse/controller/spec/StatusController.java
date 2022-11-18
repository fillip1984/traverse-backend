package org.home.productivity.traverse.controller.spec;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/status")
@Slf4j
public class StatusController {

    @Value("${spring.application.name}")
    private String appName;

    private final Environment env;

    @Autowired
    public StatusController(Environment env) {
        this.env = env;
    }

    @GetMapping
    public ResponseEntity<String> home() {
        var msg = appName + " is up and running with profiles(s): " + Arrays.toString(env.getActiveProfiles());
        log.info(msg);
        return ResponseEntity.ok(msg);
    }
}