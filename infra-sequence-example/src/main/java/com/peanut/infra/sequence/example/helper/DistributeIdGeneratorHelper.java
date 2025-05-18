package com.peanut.infra.sequence.example.helper;

import com.peanut.infra.distributesequence.segment.SegmentService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;


/**
 * @author peanut
 * @description
 */
@Component
@Slf4j
public class DistributeIdGeneratorHelper {


    @Resource
    private SegmentService segmentService;

    private static final String SEQ_KEY = "seq-audit-manual";

    private static final Integer TOTAL_NUMBER = 16;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyMM");

    /**
     * 生成分布式id
     * 长度 19位十进制数
     * yyMM + 填充0 + sequenceId
     * @param createTime
     * @return
     */
    public Long generateId(Date createTime) {

        Long sequenceId = segmentService.tryGet(SEQ_KEY, 1000);
        if(Objects.isNull(sequenceId) || Objects.equals(sequenceId, -1L)) {
            log.error("segmentService.tryGet error, sequenceId:{}", sequenceId);
            throw new RuntimeException("segmentService.tryGet error");
        }

        // yyMM + 0补位 + sequenceId，共计16位
        String yyMmPrefix = DATE_FORMATTER.print(createTime.getTime());
        String sequenceIdStr = String.valueOf(sequenceId);
        int numOfZero = TOTAL_NUMBER - sequenceIdStr.length() - yyMmPrefix.length();
        String idSb = yyMmPrefix +
                String.join("", Collections.nCopies(numOfZero, "0")) +
                sequenceIdStr;
        return Long.parseLong(idSb);
    }


}


