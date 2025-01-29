package com.dot.project.pearless.service;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.dot.project.pearless.config.ExternalRequestProperties;
import com.dot.project.pearless.constant.CurrencyEnum;
import com.dot.project.pearless.constant.StatusEnum;
import com.dot.project.pearless.dao.entity.Transaction;
import com.dot.project.pearless.dao.entity.TransactionAccount;
import com.dot.project.pearless.dao.repository.TransactionRepository;
import com.dot.project.pearless.dto.request.TransactionRequest;
import com.dot.project.pearless.dto.response.ApiResponse;
import com.dot.project.pearless.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final ExternalRequestProperties properties;


    public ApiResponse<TransactionResponse> processTransfer(TransactionRequest transactionReq) {
        log.info("Starting transfer process. Source Account = {}, Destination Account = {}, Reference = {}",
                transactionReq.getSourceAccountNumber(), transactionReq.getDestinationAccountNumber(), transactionReq.getReference());

       
        final var sourceAccount = accountService.accountEnquiry(transactionReq.getSourceAccountNumber());
        logAccountDetails(sourceAccount, "Source");
        final var destinationAccount = accountService.accountEnquiry(transactionReq.getDestinationAccountNumber());
        logAccountDetails(destinationAccount, "Destination");

        
        final var transaction = buildTransaction(transactionReq);
        log.info("Transaction initialized: {}", transaction);

        // Validation checks
        if (isSameAccount(sourceAccount, destinationAccount)) {
            return handleValidationFailure(transaction, "Source and destination accounts cannot be the same");
        }

        if (isCurrencyMismatch(sourceAccount, transactionReq.getCurrency(), "Source")
                || isCurrencyMismatch(destinationAccount, transactionReq.getCurrency(), "Destination")) {
            return handleValidationFailure(transaction, "Currency mismatch detected");
        }

        if (hasInsufficientFunds(sourceAccount, transaction.getBilledAmount())) {
            return handleValidationFailure(transaction, "Insufficient funds in source account");
        }

        
        try {
            executeTransfer(transactionReq, sourceAccount, destinationAccount, transaction);
            log.info("Transaction completed successfully. Source Account = {}, Reference = {}",
                    sourceAccount.getAccountNumber(), transactionReq.getReference());
            return ApiResponse.success(new TransactionResponse(transaction));
        } catch (Exception e) {
           
            log.error("Error during transfer process for Reference: {}", transactionReq.getReference(), e);
            return handleTransactionFailure(transaction, StatusEnum.FAILED, "An error occurred during transaction processing");
        }
    }


    public List<Transaction> getAllScheduledTransfer(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrDestinationAccountNumber(accountNumber, accountNumber);
    }

    private void logAccountDetails(TransactionAccount account, String accountType) {
        log.info("{} Account: {}, Balance: {}, Currency: {}",
                accountType, account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    private boolean isSameAccount(TransactionAccount source, TransactionAccount destination) {
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            log.warn("Validation failed: Source and destination accounts are the same. Account = {}", source.getAccountNumber());
            return true;
        }
        return false;
    }

    private boolean isCurrencyMismatch(TransactionAccount account, CurrencyEnum expectedCurrency, String accountType) {
        if (!account.getCurrency().equals(expectedCurrency)) {
            log.warn("Validation failed: Currency mismatch for {} Account = {}, Expected = {}, Provided = {}",
                    accountType, account.getAccountNumber(), account.getCurrency(), expectedCurrency);
            return true;
        }
        return false;
    }

    private boolean hasInsufficientFunds(TransactionAccount source, BigDecimal requiredAmount) {
        if (source.getBalance().compareTo(requiredAmount) < 0) {
            log.warn("Validation failed: Insufficient funds for Source Account = {}, Balance = {}, Required = {}",
                    source.getAccountNumber(), source.getBalance(), requiredAmount);
            return true;
        }
        return false;
    }

    private void executeTransfer(TransactionRequest transactionReq, TransactionAccount sourceAccount, TransactionAccount destinationAccount, Transaction transaction) {
        log.info("Debiting Source Account: {}, Amount: {}", sourceAccount.getAccountNumber(), transactionReq.getAmount());
        accountService.debitAccount(sourceAccount, transaction.getBilledAmount());

        log.info("Crediting Destination Account: {}, Amount: {}", destinationAccount.getAccountNumber(), transaction.getAmount());
        accountService.creditAccount(destinationAccount, transaction.getAmount());

        transaction.setStatus(StatusEnum.SUCCESSFUL);
        transaction.setStatusMessage("Transaction Successful");
        transactionRepository.save(transaction);
    }

    private ApiResponse<TransactionResponse> handleValidationFailure(Transaction transaction, String errorMessage) {
        log.warn("Validation error: {}", errorMessage);
        return handleTransactionFailure(transaction, StatusEnum.FAILED, errorMessage);
    }

    private ApiResponse<TransactionResponse> handleTransactionFailure(Transaction transaction, StatusEnum status, String message) {
        transaction.setStatus(status);
        transaction.setStatusMessage(message);
        transactionRepository.save(transaction);
        log.info("Transaction failed. Status = {}, Message = {}, Reference = {}", status, message, transaction.getReference());
        return ApiResponse.error(message);
    }


    private Transaction buildTransaction(TransactionRequest transactionReq) {
        final var fee = calculateFee(transactionReq.getAmount());
        return Transaction.builder()
                .sourceAccountNumber(transactionReq.getSourceAccountNumber())
                .destinationAccountNumber(transactionReq.getDestinationAccountNumber())
                .reference(transactionReq.getReference())
                .amount(transactionReq.getAmount())
                .fee(fee)
                .billedAmount(transactionReq.getAmount().add(fee))
                .description(transactionReq.getDescription())
                .build();
    }

    private BigDecimal calculateFee(BigDecimal transactionAmount) {
        BigDecimal fee = transactionAmount.multiply(new BigDecimal(properties.getFeePercentage())); // Percentage of amount
        return fee.min(new BigDecimal(properties.getFeeCap())); // Apply cap
    }


    
    public ApiResponse<Page<TransactionResponse>> getTransactions(String status, String sourceAccountNumber,
                                                             String destinationAccountNumber, String startDate,
                                                             String endDate, Pageable pageable) {

        Specification<Transaction> specification = Specification.where(null);

        // Build the specification dynamically based on input filters
        if (StringUtils.isNotBlank(status)) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (StringUtils.isNotBlank(sourceAccountNumber)) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("sourceAccountNumber"), sourceAccountNumber));
        }

        if (StringUtils.isNotBlank(destinationAccountNumber)) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("destinationAccountNumber"), destinationAccountNumber));
        }

        if (startDate != null && endDate != null) {
            specification = specification.and((root, query, cb) -> cb.between(root.get("createdAt"), startDate, endDate));
        } else if (startDate != null) {
            LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), finalStartDate));
        } else if (endDate != null) {
            LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), finalEndDate));
        }

        // Fetch paginated results
        Page<Transaction> pagedResults = transactionRepository.findAll(specification, pageable);

        if (pagedResults.isEmpty()) {
            log.info("No transactions found for the given filters: Status={}, SourceAccount={}, DestinationAccount={}, StartDate={}, EndDate={}",
                    status, sourceAccountNumber, destinationAccountNumber, startDate, endDate);
        }

        // Map entities to DTOs
        List<TransactionResponse> transactionResponses = pagedResults.getContent()
                .stream()
                .map(TransactionResponse::new)
                .toList();

        log.info("Fetched {} transactions for the given filters.", transactionResponses.size());

        // Return paginated response
        return ApiResponse.success(new PageImpl<>(transactionResponses, pageable, pagedResults.getTotalElements()));
    }


   



  
}