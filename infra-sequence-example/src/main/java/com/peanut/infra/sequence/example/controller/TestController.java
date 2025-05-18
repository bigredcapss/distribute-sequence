package com.peanut.infra.sequence.example.controller;

import com.peanut.infra.distributesequence.common.Result;
import com.peanut.infra.distributesequence.segment.SegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author peanut
 * @description
 */
@RestController
public class TestController {

    // 直接注入SegmentService
    @Autowired
    SegmentService segmentService;

    @GetMapping("/seq")
    public Long test1() {
        // 直接使用
        Result result = segmentService.getId("seq-test");
        long id = result.getId();
        return id;
    }

    @GetMapping("/seq2")
    public Long test2() {
        // 超时使用(毫秒数)
        long id = segmentService.tryGet("seq-test",1000);
        return id;
    }

}
