package com.peanut.infra.distributesequence;

import com.peanut.infra.distributesequence.common.Result;

/**
 * @author: peanut
 * @description: IDGen
 */
public interface IDGen {
    
    Result get(String key);

    boolean init();
}
