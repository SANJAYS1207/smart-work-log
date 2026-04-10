package com.sanjay.smartlog.repository;

import com.sanjay.smartlog.entity.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateRepository extends JpaRepository<UserState, String> {
    // chatId is the primary key; findById() is sufficient for all lookups
}
