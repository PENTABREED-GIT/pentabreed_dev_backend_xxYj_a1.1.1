package com.penta.template;

import com.amazonaws.services.iot.client.*;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws AWSIotException, InterruptedException, JsonProcessingException {

        String clientEndpoint = "a1yfzad4ffddko-ats.iot.ap-northeast-2.amazonaws.com";      // use value returned by describe-endpoint --endpoint-type "iot:Data-ATS"
        String clientId = "penta_1";                                                           // replace with your own client ID. Use unique client IDs for concurrent connections.
        String certificateFile = "./certificate.pem.crt";                                   // X.509 based certificate file
        String privateKeyFile = "./private.pem.key";                                        // PKCS#1 or PKCS#8 PEM encoded private key file

        // SampleUtil.java and its dependency PrivateKeyReader.java can be copied from the sample source code.
        // Alternatively, you could load key store directly from a file - see the example included in this README.
        SampleUtil.KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
        System.out.println("pair = " + pair);
        AWSIotMqttClient client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);

        // optional parameters can be set before connect()
        client.connect();


        System.out.println("AWS IoT Core에 연결되었습니다.");

        // ----------------------------------------------------
        // 1. 메시지 구독 (Subscribe)
        // ----------------------------------------------------
        String topicToSubscribe = "my/test/topic"; // 구독할 토픽
        AWSIotQos qos = AWSIotQos.QOS0; // QoS 레벨 (QOS0, QOS1, QOS2 중 선택)

        // 구독 메시지를 처리할 커스텀 토픽 클래스 정의
        AWSIotTopic topic = new AWSIotTopic(topicToSubscribe, qos) {
            @Override
            public void onMessage(AWSIotMessage message) {
                // 메시지가 도착했을 때 실행될 로직
                System.out.println(
                    String.format("수신된 메시지: 토픽 = %s, 페이로드 = %s",
                        message.getTopic(),
                        new String(message.getPayload())
                    )
                );
                // 여기에 필요한 추가 로직을 구현합니다 (예: 데이터베이스 저장, 다른 서비스 호출 등)
            }
        };

        // 토픽 구독 요청
        client.subscribe(topic, true); // 두 번째 인자는 cleanSession (true: 세션 종료 시 구독 해제, false: 세션 종료 후에도 구독 유지)
        System.out.println("토픽 '" + topicToSubscribe + "'을(를) 구독했습니다.");



        // ----------------------------------------------------
        // 2. 메시지 게시 (Publish)
        // ----------------------------------------------------
        String topicToPublish = "my/test/topic";
        AWSIotQos publishQos = AWSIotQos.QOS0; // 게시 QoS

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n메세지를 게시합니다");
        for (int i = 1; i <= 10; i++) {
            Map<String, String> map = new HashMap<>();
            String payload = "사용자가 AWS IoT Core 메세지 보내요 ~ " + i + "번째";
            map.put("message", payload);
            System.out.println("payload = " + payload);
//            client.publish(topicToPublish, publishQos, payload.getBytes());

            client.publish(topicToPublish, publishQos, objectMapper.writeValueAsString(map));
        }
        System.out.println("메세지 게시가 완료 되었습니다");

        // 애플리케이션이 바로 종료되지 않도록 잠시 대기 (구독 메시지 수신 대기)
        Thread.sleep(600000); // 10분 동안 대기. 필요에 따라 더 길게 설정하거나, 무한 루프로 유지할 수 있습니다.

        // ----------------------------------------------------
        // 2. 연결 종료 (선택 사항)
        // ----------------------------------------------------
        // 애플리케이션이 종료될 때 또는 더 이상 통신이 필요 없을 때 연결을 해제합니다.
        client.disconnect();
        System.out.println("AWS IoT Core 연결이 해제되었습니다.");
    }
}
