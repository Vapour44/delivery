package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String region;
    private String bucketName;

}
