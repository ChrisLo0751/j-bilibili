package com.bilibili;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestDao {
    public Long query(Long id);
}
