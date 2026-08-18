package com.example.studdata.dao;

public interface StudentStatistics {

    Long getTotal();

    Long getActive();

    Long getExpired();

    Long getExpiring();
}