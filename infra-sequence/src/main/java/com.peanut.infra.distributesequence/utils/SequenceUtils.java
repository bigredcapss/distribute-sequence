package com.peanut.infra.distributesequence.utils;

import com.peanut.infra.distributesequence.segment.SegmentService;

public class SequenceUtils {

    public static long getId(String key) {
        return SpringContextHolder.getBean(SegmentService.class).getId(key).getId();
    }
}
