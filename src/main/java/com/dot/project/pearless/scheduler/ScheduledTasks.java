package com.dot.project.pearless.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dot.project.pearless.config.ExternalRequestProperties;
import com.dot.project.pearless.constant.StatusEnum;
import com.dot.project.pearless.dao.entity.Transaction;
import com.dot.project.pearless.dao.repository.TransactionRepository;
import com.dot.project.pearless.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TransactionRepository transactionRepository;
    private final ExternalRequestProperties properties;
    private final TransactionService transactionService;
    public static final ZoneId ZONE_ID = ZoneId.of("Africa/Lagos");

    /**
     * Processes commissions for successful transactions from the previous day.
     * This method is scheduled to run daily at 1 AM.
     * 
     * The process involves:
     * 1. Retrieving all successful transactions from the previous day.
     * 2. Calculating and setting the commission for each transaction.
     * 3. Marking each processed transaction as commission-worthy.
     * 4. Saving the updated transaction information.
     * 
     * The commission is calculated as a percentage of the transaction fee,
     * where the percentage is defined in the application properties.
     * 
     * Any errors during the processing of individual transactions are logged,
     * but do not stop the overall process.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void processCommissions() {
        log.info("Starting commission processing task...");

        LocalDateTime yesterdayStartOfDayMidnight = getYesterdayStartOfDay();
        LocalDateTime yesterdayEndOfDay = getYesterdayEndOfDay();
        log.info("Processing commissions for transactions created between: {} and {}",
                yesterdayStartOfDayMidnight, yesterdayEndOfDay);

        List<Transaction> successfulTransactions = transactionRepository.findByStatusAndCreatedAtBetween(
                StatusEnum.SUCCESSFUL, yesterdayStartOfDayMidnight, yesterdayEndOfDay);

        log.info("Found {} successful transactions for commission processing.", successfulTransactions.size());

        for (Transaction transaction : successfulTransactions) {
            try {
                transaction.setCommissionWorthy(true);
                BigDecimal transactionFee = transaction.getFee();
                transaction.setCommission(transactionFee.multiply(new BigDecimal(properties.getCommissionPercentage()))); // 20% of transaction fee
                transactionRepository.save(transaction);
                log.info("Commission processed for transaction ID: {}", transaction.getId());
            } catch (Exception e) {
                log.error("Error processing commission for transaction ID: {} , {}", transaction.getId(), e.getMessage());
            }
        }
    }



   

    private LocalDateTime getYesterdayStartOfDay() {
        return LocalDateTime.now(ZONE_ID).with(LocalTime.MIDNIGHT).minusDays(1);
    }

    private LocalDateTime getYesterdayEndOfDay() {
        return LocalDateTime.now(ZONE_ID).with(LocalTime.of(23, 59)).minusDays(1);
    }
}
