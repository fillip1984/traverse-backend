package org.home.productivity.traverse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/traverse")
@Slf4j
public class TraverseController {

    @GetMapping
    public ResponseEntity<String> traverse() {
        log.info("Traversing...");
        return ResponseEntity.ok("success");
    }
}
