package com.dot.project.pearless.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.dot.project.pearless.constant.AccountStatusEnum;
import com.dot.project.pearless.dao.entity.TransactionAccount;
import com.dot.project.pearless.dao.repository.TransactionAccountRepository;
import com.dot.project.pearless.exception.AccountNotFoundException;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class AccountService {

    private final TransactionAccountRepository transactionAccountRepository;

    public TransactionAccount accountEnquiry(String accountNumber) {
        log.info("Initiating account enquiry for account number: {}", accountNumber);

        // Fetch the active account using the repository
        return transactionAccountRepository
                .getTransactionAccountByAccountNumberAndAccountStatusIs(accountNumber, AccountStatusEnum.ACTIVE)
                .orElseThrow(() -> {
                    log.error("No active account found with number: {}", accountNumber);
                    return new AccountNotFoundException(
                            "No active account found with number: " + accountNumber);
                });
    }

    public synchronized void debitAccount(TransactionAccount transactionAccount , BigDecimal amountToDebit){
        log.info("Debiting account: {}, Current Balance: {}, Amount to Debit: {}",
                transactionAccount.getAccountNumber(), transactionAccount.getBalance(), amountToDebit);

        // Deduct the amount and update balance
        final var newBalance = transactionAccount.getBalance().subtract(amountToDebit);
        transactionAccount.setBalance(newBalance);

        // Save the updated account and return the new balance
        final var updatedBalance = transactionAccountRepository.save(transactionAccount).getBalance();
        log.info("Account debited successfully. Account: {}, New Balance: {}",
                transactionAccount.getAccountNumber(), updatedBalance);
    }

    public synchronized void creditAccount(TransactionAccount transactionAccount , BigDecimal amountToCredit){
        log.info("Crediting account: {}, Current Balance: {}, Amount to Credit: {}",
                transactionAccount.getAccountNumber(), transactionAccount.getBalance(), amountToCredit);

        // add the amount and update balance
        final var newBalance = transactionAccount.getBalance().add(amountToCredit);
        transactionAccount.setBalance(newBalance);

        // Save the updated account and return the new balance
        final var updatedBalance = transactionAccountRepository.save(transactionAccount).getBalance();
        log.info("Account Credited successfully. Account: {}, New Balance: {}",
                transactionAccount.getAccountNumber(), updatedBalance);
    }
}
