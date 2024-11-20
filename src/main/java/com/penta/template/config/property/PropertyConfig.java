package com.penta.template.config.property;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertyConfig {

    @Bean
    public CustomYmlProperty myYmlProperty() {
        YamlPropertiesFactoryBean propertiesFactoryBean = new YamlPropertiesFactoryBean();
        ClassPathResource classPathResource = new ClassPathResource("variable.yml");
        propertiesFactoryBean.setResources(classPathResource);
        CustomYmlProperty customYmlProperty = new CustomYmlProperty(propertiesFactoryBean);
        return customYmlProperty;
    }
}
