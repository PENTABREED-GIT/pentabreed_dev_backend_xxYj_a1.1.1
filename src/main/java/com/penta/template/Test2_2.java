package com.penta.template;

// 필요한 AWS CRT 및 IoT SDK V2 라이브러리 임포트

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/*
	implementation 'software.amazon.awssdk.iotdevicesdk:aws-iot-device-sdk:1.25.0' // 여기 그룹 ID를 수정!
    implementation 'software.amazon.awssdk.crt:aws-crt:0.38.3'
    implementation 'software.amazon.awssdk:iot:2.31.45'   // 최신 버전 확인 가능
    implementation 'software.amazon.awssdk:iam:2.31.45'
    implementation 'software.amazon.awssdk:sts:2.31.45'
    implementation 'software.amazon.awssdk:sqs:2.31.45'
 */
public class Test2_2 {


    // CI/CD 환경에서 실행되는지 여부를 확인 (예외 처리를 다르게 하기 위함)
    static String ciPropValue = System.getProperty("aws.crt.ci");
    static boolean isCI = ciPropValue != null && Boolean.valueOf(ciPropValue);

    /*
     * 애플리케이션 실행 중 예외 발생 시 호출됩니다.
     * CI/CD 환경에서는 예외를 다시 던져 빌드를 실패시키고,
     * 일반 실행 시에는 예외를 출력하고 계속 진행.
     */
    static void onApplicationFailure(Throwable cause) {
        if (isCI) {
            throw new RuntimeException("BasicPubSub 실행 실패", cause);
        } else if (cause != null) {
            System.out.println("예외 발생: " + cause.toString());
            cause.printStackTrace(); // 디버깅을 위해 스택 트레이스 출력
        }
    }

    public static void main(String[] args) {

        // =====================================================================
        // AWS IoT 연결 및 MQTT 통신에 필요한 정보를 직접 변수로 설정.
        // =====================================================================
        String clientEndpoint = "a1yfzad4ffddko-ats.iot.ap-northeast-2.amazonaws.com";  // AWS IoT Core 엔드포인트 URL
//        String clientId = "penta_1";                                                  // 고유한 클라이언트 ID
        String clientId = "penta_" + UUID.randomUUID().toString();                      // 고유한 클라이언트 ID
        String certificateFile = "./certificate.pem.crt";                               // X.509 인증서 파일 경로
        String privateKeyFile = "./private.pem.key";                                    // 프라이빗 키 파일 경로
        String caFile = "./AmazonRootCA1.pem";                                          // CA 인증서 파일 경로 (필요 없으면 빈 문자열)
//        String topic = "$aws/things/obj1/shadow/name/shadow_test1/update";            // 메시지를 게시/구독할 토픽
        String topic = "my/test/topic";                                                 // 메시지를 게시/구독할 토픽
        String messagePayload = "Hello from Java V2 SDK!";                              // 보낼 메시지 내용
        int messageCount = 5;                                                           // 보낼 메시지 개수 (테스트용)
        short port = 8883;                                                              // MQTT TLS 포트

        // 프록시 설정 (필요 없으면 비워둠)
        String proxyHost = "";
        int proxyPort = 0;


        // MQTT 연결 이벤트 콜백 정의
        // 연결이 중단되거나 재개될 때 호출되는 로직을 정의.
        MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("연결이 중단되었습니다: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("연결이 재개되었습니다: " + (sessionPresent ? "기존 세션" : "새 세션"));
            }
        };

        MqttClientConnection connection = null; // finally 블록에서 connection을 닫기 위해 try 블록 밖에서 선언

        try {
            /**
             * AwsIotMqttConnectionBuilder를 사용하여 MQTT 연결을 생성.
             * X.509 인증서와 프라이빗 키 파일 경로를 기반으로 연결을 설정.
             */
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certificateFile, privateKeyFile);

            // CA 인증서 파일 경로가 지정되어 있다면 빌더에 추가.
            if (!caFile.isEmpty()) {
                builder.withCertificateAuthorityFromPath(null, caFile);
            }

            // MQTT 연결 설정을 추가.
            builder.withConnectionEventCallbacks(callbacks)  // 위에서 정의한 콜백 함수 연결
                .withClientId(clientId)                     // 클라이언트 ID 설정
                .withEndpoint(clientEndpoint)               // AWS IoT Core 엔드포인트 설정
                .withPort(port)                             // 포트 설정 (기본 8883)
                .withCleanSession(true)                     // 클린 세션 사용 (연결 해제 시 세션 상태 제거)
                .withProtocolOperationTimeoutMs(60000);     // 프로토콜 작업 타임아웃 (60초)

            // HTTP 프록시가 설정되어 있다면 빌더에 추가.
            if (!proxyHost.isEmpty() && proxyPort > 0) {
                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(proxyHost);
                proxyOptions.setPort(proxyPort);
                builder.withHttpProxyOptions(proxyOptions);
            }

            // 설정된 빌더를 바탕으로 MQTT 클라이언트 연결 객체를 생성.
            connection = builder.build(); // connection 객체 초기화

            // 빌더는 더 이상 필요 없으므로 리소스를 즉시 해제.
            builder.close();

            // MQTT 클라이언트 연결을 시도하고, 연결이 완료될 때까지 기다립니다.
            CompletableFuture<Boolean> connected = connection.connect();

            try {
                boolean sessionPresent = connected.get(); // 연결 결과를 기다립니다.
                System.out.println("AWS IoT Core에 연결되었습니다! " + (!sessionPresent ? "새로운" : "기존") + " 세션입니다.");
            } catch (Exception ex) {
                // 연결 실패 시 예외를 발생시킵니다.
                throw new RuntimeException("AWS IoT Core 연결 중 예외 발생", ex);
            }

            /////////////////////////////////
            // 지정된 토픽을 구독.
            /////////////////////////////////
            // QualityOfService.AT_LEAST_ONCE는 QoS 1 (최소 한 번 전달)을 의미.
            // 메시지 수신 시 호출되는 콜백 함수는 그대로 유지됩니다.
            CompletableFuture<Integer> subscribed = connection.subscribe(topic, QualityOfService.AT_LEAST_ONCE, (message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("메시지 수신됨: 토픽 = '" + message.getTopic() + "', 페이로드 = '" + payload + "'");
                // 실무에서는 여기서 받은 메시지를 처리하는 비즈니스 로직을 수행.
                // 예: 데이터베이스 저장, 다른 서비스 호출 등
            });
            subscribed.get();// 구독 완료를 기다립니다.
            System.out.println("토픽 '" + topic + "'을(를) 구독했습니다.");
//            Thread.sleep(600000);

            /////////////////////////////////
            // 메시지 게시 루프
            /////////////////////////////////
            System.out.println("\n메시지 게시를 시작.");
            ObjectMapper objectMapper = new ObjectMapper();
            int count = 0;
            while (count++ < messageCount) {
                // 설정된 메시지 개수만큼 반복.
                // 게시할 메시지 내용에 일련번호를 추가.

                Map<String, String> map = new HashMap<>();
                String payload = "[v2-2] 사용자가 AWS IoT Core 메세지 보내요 ~ " + (count) + "번째";
                map.put("message", payload);

//                String currentMessage = messagePayload + " #" + (count - 1);
                String currentMessage = objectMapper.writeValueAsString(map);
                // MqttMessage 객체를 생성하고 토픽에 게시.
                // QualityOfService.AT_LEAST_ONCE는 QoS 1 (최소 한 번 전달)
                // 'false'는 retain 플래그를 비활성화 (메시지가 브로커에 저장되지 않음)
                CompletableFuture<Integer> published = connection.publish(
                    new MqttMessage(topic, currentMessage.getBytes(StandardCharsets.UTF_8), QualityOfService.AT_LEAST_ONCE, false)
                );
                published.get(); // 게시 완료를 기다립니다.
                System.out.println(String.format("  토픽 '%s'에 메시지 게시: %s", topic, currentMessage));
                Thread.sleep(1000); // 1초 대기 후 다음 메시지 게시 (게시 간격 조절)
            }
            System.out.println("메시지 게시가 완료되었습니다.\n");



            System.out.println("메시지 수신을 위해 10초간 대기...");
//            Thread.sleep(600000);
            Thread.sleep(1000 * 10);
            System.out.println("대기 시간이 종료되었습니다.");


            // AWS IoT Core 연결을 해제.
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get(); // 연결 해제 완료를 기다립니다.
            System.out.println("AWS IoT Core 연결이 해제되었습니다.");


//        } catch (Exception ex) {

        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            // AWS CRT 관련 예외, 스레드 인터럽트 예외, 비동기 작업 실행 예외 처리
            onApplicationFailure(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            // 모든 MQTT 연결 관련 리소스를 완전히 해제.
            // connection이 null이 아닌 경우에만 닫습니다.
            if (connection != null) {
                connection.close();
            }
            // AWS Common Runtime (CRT)의 모든 내부 리소스가 해제될 때까지 기다립니다.
            // 애플리케이션 종료 시 리소스 누수를 방지하기 위해 중요.
            CrtResource.waitForNoResources();
            System.out.println("모든 AWS IoT 작업이 완료되었습니다!");
        }
    }
}
