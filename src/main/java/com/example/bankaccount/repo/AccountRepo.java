package com.example.bankaccount.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.bankaccount.model.Account;

@Repository
public interface AccountRepo extends JpaRepository<Account, String>{
    
}