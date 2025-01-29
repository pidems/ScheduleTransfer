package com.dot.project.pearless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan("com.dot.project")
public class ExternalRequestProperties {
    private String feePercentage;  
    private String commissionPercentage; 
    private String feeCap; 
}
