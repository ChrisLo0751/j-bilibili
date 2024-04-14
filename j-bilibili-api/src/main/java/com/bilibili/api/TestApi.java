package com.bilibili.api;

import com.bilibili.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApi {

    @Autowired
    private TestService testService;

    @GetMapping("/query")
    public Long query(Long id) {
        return testService.query(id);
    }
}
