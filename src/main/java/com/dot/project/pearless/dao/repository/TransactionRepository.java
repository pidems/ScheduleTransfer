package com.dot.project.pearless.dao.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dot.project.pearless.constant.StatusEnum;
import com.dot.project.pearless.dao.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> , JpaSpecificationExecutor<Transaction> {
    @Override
    List<Transaction> findAll(Specification<Transaction> spec);
    List<Transaction> findByStatusAndCreatedAtBetween(StatusEnum status, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumber(String sourceAccountNumber, String destinationAccountNumber);
}