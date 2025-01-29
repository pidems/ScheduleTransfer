package com.dot.project.pearless;

import com.dot.project.pearless.config.ExternalRequestProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties({ExternalRequestProperties.class})
//@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableScheduling
@SpringBootApplication
public class TransferServiceAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferServiceAssessmentApplication.class, args);
    }

}
