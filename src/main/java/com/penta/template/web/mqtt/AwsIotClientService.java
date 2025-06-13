//package com.penta.template.web.mqtt;
//
//
//import com.amazonaws.services.iot.client.*;
//import jakarta.annotation.PreDestroy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.KeyStore;
//import java.time.Instant;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class AwsIotClientService {
//
//    private static final Logger logger = LoggerFactory.getLogger(AwsIotClientService.class);
//
//    private final AwsIotProperties iotProperties;
//    private AWSIotMqttClient awsIotClient;
//    private ScheduledExecutorService scheduler;
//
//    public AwsIotClientService(AwsIotProperties iotProperties) {
//        this.iotProperties = iotProperties;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void init() {
//        try {
//            logger.info("AWS IoT 클라이언트 초기화 중...");
//
//            // 리소스 폴더에서 파일 로드 (getResourceAsStream 사용)
//            InputStream certificateStream = getClass().getClassLoader().getResourceAsStream(iotProperties.getCertificateFile());
//            InputStream privateKeyStream = getClass().getClassLoader().getResourceAsStream(iotProperties.getPrivateKeyFile());
//            InputStream rootCaStream = getClass().getClassLoader().getResourceAsStream(iotProperties.getRootCaFile());
//
//            if (certificateStream == null || privateKeyStream == null || rootCaStream == null) {
//                throw new IOException("인증서/키 파일 또는 CA 파일이 src/main/resources에 없거나 이름을 잘못 지정했습니다.");
//            }
//
//            // SampleUtil을 사용하여 KeyStore 로드 (아래에 SampleUtil 코드 제공)
//            SampleUtil.KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateStream, privateKeyStream);
//            KeyStore clientKeyStore = pair.keyStore;
//            String keyStorePassword = pair.keyPassword;
//            KeyStore trustStore = SampleUtil.getTrustStoreFromX509(rootCaStream);
//
//            awsIotClient = new AWSIotMqttClient(
//                    iotProperties.getEndpoint(),
//                    iotProperties.getClientId(),
//                    clientKeyStore,
//                    keyStorePassword,
//                    trustStore
//            );
//
//            awsIotClient.connect();
//            logger.info("AWS IoT Core에 연결되었습니다!");
//
//            // 구독
//            awsIotClient.subscribe(new AWSIotTopic(iotProperties.getSubscribeTopic(), AWSIotQos.QOS0) {
//                @Override
//                public void onMessage(AWSIotMessage message) {
//                    try {
//                        String receivedPayload = new String(message.getPayload(), "UTF-8");
//                        logger.info("메시지 수신 (토픽: {}): {}", message.getTopic(), receivedPayload);
//                        // 수신된 명령 처리 로직
//                        if (receivedPayload.contains("reboot")) {
//                            logger.info("명령 수신: 장치 {} 재부팅...", iotProperties.getClientId());
//                        }
//                    } catch (Exception e) {
//                        logger.error("메시지 처리 오류: {}", e.getMessage(), e);
//                    }
//                }
//            });
//            logger.info("토픽 구독 완료: {}", iotProperties.getSubscribeTopic());
//
//            // 주기적인 발행 시작
//            scheduler = Executors.newSingleThreadScheduledExecutor();
//            scheduler.scheduleAtFixedRate(this::publishMessage, 0, iotProperties.getPublishIntervalMs(), TimeUnit.MILLISECONDS);
//            logger.info("메시지 발행 스케줄러 시작. 인터벌: {}ms", iotProperties.getPublishIntervalMs());
//
//        } catch (Exception e) {
//            logger.error("AWS IoT 클라이언트 초기화 중 오류 발생: {}", e.getMessage(), e);
//        }
//    }
//
//    private void publishMessage() {
//        try {
//            String payload = String.format("{\"device\": \"%s\", \"timestamp\": \"%s\", \"value\": %d, \"status\": \"active\"}",
//                    iotProperties.getClientId(),
//                    Instant.now().toString(),
//                    (int) (Math.random() * 100) // 0-99 사이의 무작위 값
//            );
//            awsIotClient.publish(iotProperties.getPublishTopic(), AWSIotQos.QOS0, payload);
//            logger.info("메시지 발행 (토픽: {}): {}", iotProperties.getPublishTopic(), payload);
//        } catch (AWSIotException e) {
//            logger.error("메시지 발행 오류: {}", e.getMessage(), e);
//        }
//    }
//
//    @PreDestroy
//    public void cleanup() {
//        if (scheduler != null && !scheduler.isShutdown()) {
//            scheduler.shutdown();
//            logger.info("메시지 발행 스케줄러 종료.");
//        }
//        if (awsIotClient != null && awsIotClient.getConnectionStatus().equals(AWSIotMqttClient.ConnectionStatus.CONNECTED)) {
//            try {
//                awsIotClient.disconnect();
//                logger.info("AWS IoT Core 연결 해제.");
//            } catch (AWSIotException e) {
//                logger.error("AWS IoT Core 연결 해제 오류: {}", e.getMessage(), e);
//            }
//        }
//    }
//}
