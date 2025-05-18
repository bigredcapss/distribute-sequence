package com.peanut.infra.distributesequence.segment.dao;

import com.peanut.infra.distributesequence.segment.model.SequenceAlloc;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface SequenceAllocMapper {

    @Select("SELECT biz_tag, max_id, step, update_time FROM peanut_sequence")
    @Results(value = {
            @Result(column = "biz_tag", property = "key"),
            @Result(column = "max_id", property = "maxId"),
            @Result(column = "step", property = "step"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<SequenceAlloc> getAllSequenceAllocs();

    @Select("SELECT biz_tag, max_id, step FROM peanut_sequence WHERE biz_tag = #{tag}")
    @Results(value = {
            @Result(column = "biz_tag", property = "key"),
            @Result(column = "max_id", property = "maxId"),
            @Result(column = "step", property = "step")
    })
    SequenceAlloc getSequenceAlloc(@Param("tag") String tag);

    @Update("UPDATE peanut_sequence SET max_id = max_id + step WHERE biz_tag = #{tag}")
    void updateMaxId(@Param("tag") String tag);

    @Update("UPDATE peanut_sequence SET max_id = max_id + #{step} WHERE biz_tag = #{key}")
    void updateMaxIdByCustomStep(@Param("sequenceAlloc") SequenceAlloc sequenceAlloc);

    @Select("SELECT biz_tag FROM peanut_sequence")
    List<String> getAllTags();
}
