package com.dot.project.pearless.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dot.project.pearless.constant.AccountStatusEnum;
import com.dot.project.pearless.dao.entity.TransactionAccount;

import java.util.Optional;

public interface TransactionAccountRepository extends JpaRepository<TransactionAccount, Long> {
  Optional<TransactionAccount> getTransactionAccountByAccountNumberAndAccountStatusIs(String accountName, AccountStatusEnum accountStatus);
}