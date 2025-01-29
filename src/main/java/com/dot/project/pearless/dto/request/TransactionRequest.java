package com.dot.project.pearless.dto.request;

import com.dot.project.pearless.constant.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest implements Serializable {

    @NotBlank(message = "Transaction reference is required")
    private String reference;

    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction currency is required")
    private CurrencyEnum currency;

    private String description;

    @NotBlank(message = "account number is required")
    @Size(min = 10, max = 20 , message = "invalid source account number")
    private String sourceAccountNumber;

    @NotBlank(message = "account number is required")
    @Size(min = 10, max = 20 , message = "invalid destination account number")
    private String destinationAccountNumber;
}