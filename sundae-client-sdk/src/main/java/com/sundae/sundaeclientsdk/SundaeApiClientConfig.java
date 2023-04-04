package com.sundae.sundaeclientsdk;

import com.sundae.sundaeclientsdk.client.SundaeApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sundaeapi.client")
@Data
@ComponentScan
public class SundaeApiClientConfig {

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * 私钥
     */
    private String secretKey;

    @Bean
    public SundaeApiClient sundaeApiClient() {
        return new SundaeApiClient(accessKey, secretKey);
    }
}
