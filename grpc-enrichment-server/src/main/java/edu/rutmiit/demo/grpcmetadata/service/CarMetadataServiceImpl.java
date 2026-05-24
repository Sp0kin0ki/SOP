package edu.rutmiit.demo.grpcmetadata.service;

import edu.rutmiit.demo.grpc.CarMetadataServiceGrpc;
import edu.rutmiit.demo.grpc.ResolveSpecificationsRequest;
import edu.rutmiit.demo.grpc.ResolveSpecificationsResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Реализация gRPC-сервиса CarMetadataService.
 *
 * Сервис обогащает автомобиль metadata-характеристиками на основе
 * brand / model / year и возвращает их в response.
 *
 * - наследование от generated ImplBase
 * - unary RPC
 * - логирование request/response
 * - формирование ответа через builder
 */
public class CarMetadataServiceImpl extends CarMetadataServiceGrpc.CarMetadataServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CarMetadataServiceImpl.class);

    @Override
    public void resolveSpecifications(
            ResolveSpecificationsRequest request,
            StreamObserver<ResolveSpecificationsResponse> responseObserver) {

        log.info("gRPC request: car id={} brand={} model={} year={}",
                request.getCarId(),
                request.getBrand(),
                request.getModel(),
                request.getYear());

        ResolveSpecificationsResponse response = buildResponse(request);

        log.info("gRPC response: car id={} engine={} hp={} bodyType={} transmission={} drivetrain={}",
                response.getCarId(),
                response.getEngine(),
                response.getHorsepower(),
                response.getBodyType(),
                response.getTransmission(),
                response.getDrivetrain());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private ResolveSpecificationsResponse buildResponse(ResolveSpecificationsRequest request) {
        String brand = normalize(request.getBrand());
        String model = normalize(request.getModel());
        int year = request.getYear();

        ResolveSpecificationsResponse.Builder builder = ResolveSpecificationsResponse.newBuilder()
                .setCarId(request.getCarId());

        if (brand.equals("bmw")) {
            builder.setEngine("4.4 V8")
                    .setHorsepower(625)
                    .setBodyType("Sedan")
                    .setTransmission("Automatic")
                    .setDrivetrain("AWD")
                    .putSpecifications("country", "Germany")
                    .putSpecifications("segment", "premium");

            if (model.contains("m5")) {
                builder.putSpecifications("series", "M");
            }
        }
        else if (brand.equals("audi")) {
            builder.setEngine("4.0 V8")
                    .setHorsepower(591)
                    .setBodyType("Sportback")
                    .setTransmission("Automatic")
                    .setDrivetrain("AWD")
                    .putSpecifications("country", "Germany")
                    .putSpecifications("segment", "premium");

            if (model.contains("rs7")) {
                builder.putSpecifications("series", "RS");
            }
        }
        else if (brand.equals("mercedes") || brand.equals("mercedes-benz")) {
            builder.setEngine("4.0 V8")
                    .setHorsepower(630)
                    .setBodyType("Coupe")
                    .setTransmission("Automatic")
                    .setDrivetrain("RWD")
                    .putSpecifications("country", "Germany")
                    .putSpecifications("segment", "premium");
        }
        else if (brand.equals("toyota")) {
            builder.setEngine("2.5 I4")
                    .setHorsepower(203)
                    .setBodyType("Sedan")
                    .setTransmission("Automatic")
                    .setDrivetrain("FWD")
                    .putSpecifications("country", "Japan")
                    .putSpecifications("segment", "mass-market");
        }
        else {
            builder.setEngine("Unknown")
                    .setHorsepower(0)
                    .setBodyType("Unknown")
                    .setTransmission("Unknown")
                    .setDrivetrain("Unknown")
                    .putSpecifications("country", "Unknown")
                    .putSpecifications("segment", "unknown");
        }

        if (year >= 2020) {
            builder.putSpecifications("generation", "modern");
        } else if (year >= 2010) {
            builder.putSpecifications("generation", "late");
        } else if (year > 0) {
            builder.putSpecifications("generation", "classic");
        }

        return builder.build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}