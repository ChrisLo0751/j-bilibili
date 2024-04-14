package com.bilibili.service;

import com.bilibili.dao.TestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private TestDao testDao;

    public Long query(Long id) {
        return  testDao.query(id);
    }
}
