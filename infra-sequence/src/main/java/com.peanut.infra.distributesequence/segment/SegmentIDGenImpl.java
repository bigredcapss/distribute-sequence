package com.peanut.infra.distributesequence.segment;

import com.peanut.infra.distributesequence.IDGen;
import com.peanut.infra.distributesequence.common.Result;
import com.peanut.infra.distributesequence.common.Status;
import com.peanut.infra.distributesequence.exception.ExceptionCode;
import com.peanut.infra.distributesequence.segment.cache.Segment;
import com.peanut.infra.distributesequence.segment.cache.SegmentBuffer;
import com.peanut.infra.distributesequence.segment.dao.SequenceAllocDao;
import com.peanut.infra.distributesequence.segment.model.SequenceAlloc;
import com.peanut.infra.distributesequence.utils.SequenceThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SegmentIDGenImpl implements IDGen {

    private static final Logger logger = LoggerFactory.getLogger(SegmentIDGenImpl.class);

    private volatile boolean initOK = false;

    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<String, SegmentBuffer>();

    private SequenceAllocDao dao;

    private static final int MAX_STEP = 1000000;

    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;

    private ExecutorService segmentSyncExecutor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new SequenceThreadFactory("segment-update-"));

    @Override
    public boolean init() {
        logger.info("Init ...");
        updateCacheFromDb();
        initOK = true;
        updateCacheFromDbAtEveryMinute();
        return initOK;
    }

    private void updateCacheFromDb() {
        logger.info("update cache from db");
        try {
            List<String> dbTags = dao.getAllTags();
            if (dbTags == null || dbTags.isEmpty()) {
                return;
            }
            List<String> cacheTags = new ArrayList<String>(cache.keySet());
            Set<String> insertTagsSet = new HashSet<>(dbTags);
            Set<String> removeTagsSet = new HashSet<>(cacheTags);
            //db
            for (int i = 0; i < cacheTags.size(); i++) {
                String tmp = cacheTags.get(i);
                if (insertTagsSet.contains(tmp)) {
                    insertTagsSet.remove(tmp);
                }
            }
            for (String tag : insertTagsSet) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                logger.info("Add tag {} from db to IdCache, SegmentBuffer {}", tag, buffer);
            }
            //cache
            for (int i = 0; i < dbTags.size(); i++) {
                String tmp = dbTags.get(i);
                if (removeTagsSet.contains(tmp)) {
                    removeTagsSet.remove(tmp);
                }
            }
            for (String tag : removeTagsSet) {
                cache.remove(tag);
                logger.info("Remove tag {} from IdCache", tag);
            }
        } catch (Exception e) {
            logger.warn("update cache from db exception", e);
        }
    }

    private void updateCacheFromDbAtEveryMinute() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("check-idCache-thread");
                t.setDaemon(true);
                return t;
            }
        });
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateCacheFromDb();
            }
        }, 60, 60, TimeUnit.SECONDS);
    }


    @Override
    public Result get(final String key) {
        if (!initOK) {
            return new Result(ExceptionCode.EXCEPTION_ID_IDCACHE_INIT_FALSE, Status.EXCEPTION);
        }
        if (cache.containsKey(key)) {
            SegmentBuffer buffer = cache.get(key);
            if (!buffer.isInitOk()) {
                synchronized (buffer) {
                    if (!buffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, buffer.getCurrent());
                            logger.info("Init buffer. Update sequence key {} {} from db", key, buffer.getCurrent());
                            buffer.setInitOk(true);
                        } catch (Exception e) {
                            logger.warn("Init buffer {} exception", buffer.getCurrent(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(cache.get(key));
        }
        return new Result(ExceptionCode.EXCEPTION_ID_KEY_NOT_EXISTS, Status.EXCEPTION);
    }


    private int getNextStep(SegmentBuffer buffer) {
        long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
        int nextStep = buffer.getStep();
        if (duration < SEGMENT_DURATION) {
            if (nextStep * 2 > MAX_STEP) {
                //do nothing
            } else {
                nextStep = nextStep * 2;
            }
        } else if (duration < SEGMENT_DURATION * 2) {
            //do nothing
        } else {
            nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
        }
        return nextStep;
    }

    public void updateSegmentFromDb(String key, Segment segment) {
        logger.info("update segment from db,key:{}", key);
        SegmentBuffer buffer = segment.getBuffer();
        SequenceAlloc sequenceAlloc;
        if (!buffer.isInitOk()) {
            sequenceAlloc = dao.updateMaxIdAndGetSequenceAlloc(key);
            buffer.setStep(sequenceAlloc.getStep());
            buffer.setMinStep(sequenceAlloc.getStep());
        } else if (buffer.getUpdateTimestamp() == 0) {
            sequenceAlloc = dao.updateMaxIdAndGetSequenceAlloc(key);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(sequenceAlloc.getStep());
            buffer.setMinStep(sequenceAlloc.getStep());
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = getNextStep(buffer);
            logger.info("sequence key[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), String.format("%.2f",
                    ((double) duration / (1000 * 60))), nextStep);

            SequenceAlloc temp = new SequenceAlloc();
            temp.setKey(key);
            temp.setStep(nextStep);
            sequenceAlloc = dao.updateMaxIdByCustomStepAndGetSequenceAlloc(temp);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(sequenceAlloc.getStep());
        }
        long value = sequenceAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(sequenceAlloc.getMaxId());
        segment.setStep(buffer.getStep());
    }


    public Result getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep()) && buffer.getThreadRunning().compareAndSet(false, true)) {
                    segmentSyncExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Segment next = buffer.getSegments()[buffer.nextPos()];
                            boolean updateOk = false;
                            try {
                                updateSegmentFromDb(buffer.getKey(), next);
                                updateOk = true;
                                logger.info("update segment {} from db {}", buffer.getKey(), next);
                            } catch (Exception e) {
                                logger.warn(buffer.getKey() + " updateSegmentFromDb exception", e);
                            } finally {
                                if (updateOk) {
                                    buffer.wLock().lock();
                                    buffer.setNextReady(true);
                                    buffer.getThreadRunning().set(false);
                                    buffer.wLock().unlock();
                                } else {
                                    buffer.getThreadRunning().set(false);
                                }
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, Status.SUCCESS);
                }
            } finally {
                buffer.rLock().unlock();
            }
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, Status.SUCCESS);
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    logger.error("Both two segments in {} are not ready!", buffer);
                    return new Result(ExceptionCode.EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL, Status.EXCEPTION);
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    public List<SequenceAlloc> getAllSequenceAllocs() {
        return dao.getAllSequenceAllocs();
    }

    public Map<String, SegmentBuffer> getCache() {
        return cache;
    }

    public SequenceAllocDao getDao() {
        return dao;
    }

    public void setDao(SequenceAllocDao dao) {
        this.dao = dao;
    }
}