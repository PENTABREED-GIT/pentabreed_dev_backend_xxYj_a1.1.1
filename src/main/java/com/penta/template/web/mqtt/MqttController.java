package com.penta.template.web.mqtt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttController {

    @GetMapping("/mqtt")
    public void mqtt() {
        System.out.println("MqttController.mqtt");
    }
}
