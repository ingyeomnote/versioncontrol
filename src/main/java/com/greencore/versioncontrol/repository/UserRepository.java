package com.greencore.versioncontrol.repository;

import com.greencore.versioncontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username); //  findByUsername에 맞는 쿼리 생성
}
