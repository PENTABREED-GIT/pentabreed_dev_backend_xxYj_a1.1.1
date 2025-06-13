package com.penta.template;

import com.penta.template.mqtt.CommandLineUtils;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class Test2_back {

    // CI/CD 환경에서 실행되는지 여부를 확인 (예외 처리를 다르게 하기 위함)
    static String ciPropValue = System.getProperty("aws.crt.ci");
    static boolean isCI = ciPropValue != null && Boolean.valueOf(ciPropValue);

    static CommandLineUtils cmdUtils; // 커맨드 라인 유틸리티 인스턴스 (메인 메서드에서 초기화 됨)

    /*
     * CI/CD 환경에서 호출되면 예외를 발생시켜 작업을 실패시킵니다.
     * 그 외의 경우에는 발생한 문제(있다면)를 출력하고 계속 진행합니다 (main에서 리턴).
     */
    static void onApplicationFailure(Throwable cause) {
        if (isCI) {
            throw new RuntimeException("BasicPubSub 실행 실패", cause);
        } else if (cause != null) {
            System.out.println("예외 발생: " + cause.toString());
            cause.printStackTrace(); // 스택 트레이스 출력하여 디버깅에 도움
        }
    }

    public static void main(String[] args) {

        /**
         * cmdData는 커맨드 라인에서 입력받은 인수를 단일 구조체에 담아 샘플에서 사용하도록 합니다.
         * 이는 모든 커맨드 라인 파싱, 유효성 검사 등을 처리합니다.
         * Utils/CommandLineUtils에서 더 많은 정보를 확인하세요.
         */
        // 아래 줄은 예제에서 커맨드 라인 인수를 받는 부분입니다.
        // 실제 애플리케이션에서는 이 부분을 직접 구성해야 합니다.
        // 예를 들어, 하드코딩하거나 설정 파일에서 읽어올 수 있습니다.
        CommandLineUtils.SampleCommandLineData cmdData = CommandLineUtils.getInputForIoTSample("PubSub", args);

        // MQTT 연결 이벤트 콜백 정의 (연결 중단, 재개 시 호출됨)
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

        try {

            /**
             * 빌더를 사용하여 MQTT 연결을 생성합니다.
             * X.509 인증서 및 프라이빗 키 파일 경로를 사용하여 연결을 설정합니다.
             */
            // 인증서와 프라이빗 키 파일 경로를 직접 지정 (예: "./certificate.pem.crt", "./private.pem.key")
            // cmdData.input_cert와 cmdData.input_key는 CommandLineUtils에서 파싱된 값입니다.
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(cmdData.input_cert, cmdData.input_key);

            // CA 인증서 파일이 있다면 추가 (선택 사항)
            if (cmdData.input_ca != "") {
                builder.withCertificateAuthorityFromPath(null, cmdData.input_ca);
            }

            builder.withConnectionEventCallbacks(callbacks) // 위에서 정의한 콜백 설정
                .withClientId(cmdData.input_clientId) // 클라이언트 ID (예: "sdk-java" 또는 "client_...")
                .withEndpoint(cmdData.input_endpoint) // AWS IoT Core 엔드포인트 URL
                .withPort(cmdData.input_port) // 포트 (기본 8883)
                .withCleanSession(true) // 클린 세션 사용 여부 (true: 세션 상태 저장 안함)
                .withProtocolOperationTimeoutMs(60000); // 프로토콜 작업 타임아웃 (밀리초)

            // HTTP 프록시 설정 (필요한 경우)
            if (cmdData.input_proxyHost != "" && cmdData.input_proxyPort > 0) {
                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(cmdData.input_proxyHost);
                proxyOptions.setPort(cmdData.input_proxyPort);
                builder.withHttpProxyOptions(proxyOptions);
            }

            // MQTT 클라이언트 연결 객체 빌드
            MqttClientConnection connection = builder.build();
            // 빌더는 더 이상 필요 없으므로 닫습니다. (리소스 해제)
            builder.close();

            // MQTT 클라이언트 연결 시도
            CompletableFuture<Boolean> connected = connection.connect();
            try {
                boolean sessionPresent = connected.get(); // 연결 완료 대기
                System.out.println("연결 성공! " + (!sessionPresent ? "새로운" : "기존") + " 세션입니다.");
            } catch (Exception ex) {
                throw new RuntimeException("연결 중 예외 발생", ex);
            }

            // 토픽 구독
            CountDownLatch countDownLatch = new CountDownLatch(cmdData.input_count); // 메시지 수신 대기용 Latch
            // QualityOfService.AT_LEAST_ONCE 는 QoS 1을 의미합니다. (최소 한 번 전달)
            // QualityOfService.AT_MOST_ONCE 는 QoS 0을 의미합니다.
            CompletableFuture<Integer> subscribed = connection.subscribe(cmdData.input_topic, QualityOfService.AT_LEAST_ONCE, (message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("메시지 수신됨: " + payload);
                countDownLatch.countDown(); // 메시지 수신 시 카운트 다운
            });
            subscribed.get(); // 구독 완료 대기
            System.out.println("토픽 '" + cmdData.input_topic + "'을(를) 구독했습니다.");


            // 토픽에 메시지 게시
            System.out.println("\n메시지 게시를 시작합니다.");
            int count = 0;
            while (count++ < cmdData.input_count) { // cmdData.input_count 만큼 메시지 게시
                // MqttMessage 객체 생성 (토픽, 페이로드, QoS, retain)
                // QualityOfService.AT_LEAST_ONCE는 QoS 1 (최소 한 번 전달)
                // false는 retain 플래그를 비활성화 (메시지가 브로커에 저장되지 않음)
                CompletableFuture<Integer> published = connection.publish(new MqttMessage(cmdData.input_topic, cmdData.input_message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
                published.get(); // 게시 완료 대기
                System.out.println(String.format("  토픽 '%s'에 메시지 게시: %s", cmdData.input_topic, cmdData.input_message));
                Thread.sleep(1000); // 1초 대기
            }
            System.out.println("메시지 게시가 완료되었습니다.\n");

            // 구독된 메시지가 모두 수신될 때까지 대기
            // (게시된 메시지를 자기 자신이 다시 수신하는 경우에 유용)
            // cmdData.input_count만큼 메시지를 게시하고, 그만큼 메시지를 받아야 다음으로 넘어감
            countDownLatch.await();

            // 연결 해제
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get(); // 연결 해제 완료 대기
            System.out.println("AWS IoT Core 연결이 해제되었습니다.");

            // 연결 리소스 완전히 해제
            connection.close();

        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            // CRT 관련 예외, 인터럽트 예외, 실행 예외 처리
            onApplicationFailure(ex);
        } finally {
            // 모든 CRT 리소스가 해제될 때까지 대기 (필수)
            CrtResource.waitForNoResources();
            System.out.println("모든 작업 완료!");
        }
    }
}
