package edu.rutmiit.demo.grpcenrichment.config;

import edu.rutmiit.demo.grpc.CarMetadataServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Конфигурация gRPC-клиента — создание канала и стаба.
 * ManagedChannel — соединение с gRPC-сервером. Аналог HttpClient для REST.
 * - Управляет пулом TCP-соединений (HTTP/2 мультиплексирование)
 * - Поддерживает reconnect при обрыве
 * - Требует shutdown при завершении приложения (утечка ресурсов!)
 *
 * BlockingStub — синхронный клиентский стаб. Аналог RestTemplate для REST.
 * - Вызов метода блокирует поток до получения ответа
 * - Подходит для простых unary RPC
 * - Для async используется BookAnalyticsGrpc.newFutureStub() или newStub()
 *
 * usePlaintext() — отключает TLS (В проде обязательно TLS с сертификатами).
 * Для лабы упростили...
 */
@Configuration
public class GrpcClientConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientConfig.class);

    @Value("${grpc.client.metadata-server.host:localhost}")
    private String grpcHost;

    @Value("${grpc.client.metadata-server.port:9090}")
    private int grpcPort;

    private ManagedChannel channel;

    /**
     * ManagedChannel — HTTP/2 соединение с gRPC-сервером.
     *
     * Один канал может обслуживать множество одновременных RPC-вызовов
     * благодаря мультиплексированию HTTP/2 (в отличие от HTTP/1.1,
     * где каждый запрос требует отдельного TCP-соединения).
     */
    @Bean
    public ManagedChannel managedChannel() {
        channel = ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .usePlaintext()  // Без TLS — только для разработки!
                .build();

        log.info("gRPC канал создан: {}:{}", grpcHost, grpcPort);
        return channel;
    }

    @Bean
    public CarMetadataServiceGrpc.CarMetadataServiceBlockingStub
    carMetadataStub(ManagedChannel channel) {

        return CarMetadataServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Корректное закрытие gRPC-канала при остановке приложения.
     * Без этого — утечка TCP-соединений.
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Закрытие gRPC канала...");
            channel.shutdown();
        }
    }
}
