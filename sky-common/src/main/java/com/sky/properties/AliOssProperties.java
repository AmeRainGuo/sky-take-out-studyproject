package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
//@ConfigurationProperties注解 用于将配置文件中的属性值注入到该类中
//prefix属性指定了配置文件中属性的前缀，只有以该前缀开头的属性才会被注入
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssProperties {
    private String endpoint;
    private String bucketName;
    private String region;
}
