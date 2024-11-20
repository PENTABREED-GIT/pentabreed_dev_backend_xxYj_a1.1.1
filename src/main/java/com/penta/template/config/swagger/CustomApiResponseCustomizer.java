package com.penta.template.config.swagger;

import com.penta.template.common.annotation.CustomApiResponse;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class CustomApiResponseCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        CustomApiResponse customApiResponse = handlerMethod.getMethodAnnotation(CustomApiResponse.class);
        if (customApiResponse != null) {
            operation.summary(customApiResponse.summary());
            operation.getResponses().get("200").setDescription(customApiResponse.successDescription());
            operation.getResponses().get("400").setDescription(customApiResponse.errorDescription());
        }
        return operation;
    }
}
