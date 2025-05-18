package com.peanut.infra.distributesequence.segment.dao;

import com.peanut.infra.distributesequence.segment.model.SequenceAlloc;

import java.util.List;

public interface SequenceAllocDao {

    List<SequenceAlloc> getAllSequenceAllocs();

    SequenceAlloc updateMaxIdAndGetSequenceAlloc(String tag);

    SequenceAlloc updateMaxIdByCustomStepAndGetSequenceAlloc(SequenceAlloc sequenceAlloc);

    List<String> getAllTags();
}