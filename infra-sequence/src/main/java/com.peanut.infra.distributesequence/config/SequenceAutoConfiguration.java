package com.peanut.infra.distributesequence.config;

import com.peanut.infra.distributesequence.segment.SegmentService;
import com.peanut.infra.distributesequence.utils.SpringContextHolder;
import org.springframework.context.annotation.Bean;

/**
 * @author peanut
 * @description
 */
public class SequenceAutoConfiguration {

    @Bean
    public SpringContextHolder SpringContextHolder() {
        return new SpringContextHolder();
    }

    @Bean
    public SegmentProperties SegmentProperties() {
        return new SegmentProperties();
    }

    @Bean
    public SegmentService segmentService(SegmentProperties segmentProperties) {
        return new SegmentService(segmentProperties);
    }
}

