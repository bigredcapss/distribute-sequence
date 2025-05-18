package com.peanut.infra.distributesequence.segment;

import com.peanut.infra.distributesequence.IDGen;
import com.peanut.infra.distributesequence.common.Result;
import com.peanut.infra.distributesequence.common.Status;
import com.peanut.infra.distributesequence.config.SegmentProperties;
import com.peanut.infra.distributesequence.exception.SequenceException;
import com.peanut.infra.distributesequence.segment.dao.SequenceAllocDao;
import com.peanut.infra.distributesequence.segment.dao.impl.SequenceAllocDaoImpl;
import com.peanut.infra.distributesequence.utils.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @author peanut
 * @description 分段算法
 */
public class SegmentService {

    private static Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private DataSource dataSource;

    private IDGen idGen;

    public SegmentService(SegmentProperties properties) {

        boolean flag = properties.isSequenceSegmentEnabled();
        if (flag) {
            if (DataSourceUtils.isDruidDataSource()) {
                dataSource = DataSourceUtils.createDruidDatasource(properties);
            } else if (DataSourceUtils.isHikariDataSource()) {
                dataSource = DataSourceUtils.createHikariDatasource(properties);
            }

            SequenceAllocDao dao = new SequenceAllocDaoImpl(dataSource);

            idGen = new SegmentIDGenImpl();
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new SequenceException("Segment Service Init Fail");
            }
        }
    }

    public long tryGet(String key, long timeout) {
        long leftTime = timeout;
        long beginTime = 0;
        long duration = 0;
        while (true) {
            if (leftTime < 0) {
                return -1;
            }
            beginTime = System.currentTimeMillis();
            Result result = idGen.get(key);
            if (result != null && result.getStatus().equals(Status.SUCCESS)) {
                return result.getId();
            }
            duration = System.currentTimeMillis() - beginTime;
            leftTime = leftTime - duration;
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }
}