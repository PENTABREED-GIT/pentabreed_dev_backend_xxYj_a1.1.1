package com.penta.template.config.property;


import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;

import java.util.Properties;

public class CustomYmlProperty {


    private final YamlPropertiesFactoryBean yamlBean;

    public CustomYmlProperty(YamlPropertiesFactoryBean yamlBean) {
        this.yamlBean = yamlBean;
    }


    public String getKey(String keyName) {
        Properties properties = yamlBean.getObject();
        return properties.getProperty(keyName);
    }


}
