package com.dot.project.pearless.controller;

import com.dot.project.pearless.dao.entity.Transaction;
import com.dot.project.pearless.dto.request.TransactionRequest;
import com.dot.project.pearless.dto.response.ApiResponse;
import com.dot.project.pearless.dto.response.TransactionResponse;
import com.dot.project.pearless.service.TransactionService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransferServiceController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransactionRequest transactionRequest) {
        log.info("Transfer request received: {}", transactionRequest);

        final var processedTransaction = transactionService.processTransfer(transactionRequest);
        return ResponseEntity.ok(processedTransaction);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @Pattern(
                    regexp = "^(SUCCESSFUL|INSUFFICIENT_FUND|FAILED)$",
                    message = "Invalid status. Allowed values: SUCCESSFUL, INSUFFICIENT_FUND, FAILED"
            )
            @RequestParam(required = false) String status,

            @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters!")
            @RequestParam(required = false) String sourceAccountNumber,

            @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters!")
            @RequestParam(required = false) String destinationAccountNumber,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String startDate,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String endDate,

            Pageable pageable) {
        log.info("Transaction search request: status={}, sourceAccountNumber={}, destinationAccountNumber={}, startDate={}," +
                        " endDate={}", status, sourceAccountNumber, destinationAccountNumber, startDate, endDate);

        final var transactions = transactionService.getTransactions(
                status, sourceAccountNumber, destinationAccountNumber, startDate, endDate, pageable);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getScheduledTransfers(@PathVariable String accountId) {
        List<Transaction> transfers = transactionService.getAllScheduledTransfer(accountId);
        return ResponseEntity.ok(transfers);
    }
}

