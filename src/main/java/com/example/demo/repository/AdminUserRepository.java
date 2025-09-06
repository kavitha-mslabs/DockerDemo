package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.AdminUserBean;



@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserBean,Long>{

    AdminUserBean findByUserName(String userName);

}
