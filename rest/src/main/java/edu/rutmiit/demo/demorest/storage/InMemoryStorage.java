package edu.rutmiit.demo.demorest.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import edu.rutmiit.demo.carsapicontract.dto.CarResponse;
import edu.rutmiit.demo.carsapicontract.dto.DealerResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {

    public final Map<Long, CarResponse> cars = new ConcurrentHashMap<>();
    public final Map<Long, DealerResponse> dealers = new ConcurrentHashMap<>();

    public final AtomicLong carSequence = new AtomicLong(0);
    public final AtomicLong dealerSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {

        // --- Dealers ---
        DealerResponse dealer1 = DealerResponse.builder()
                .id(dealerSequence.incrementAndGet())
                .city("Warsaw")
                .address("Marszałkowska 10")
                .phone("+48123123123")
                .carsCount(2)
                .build();

        DealerResponse dealer2 = DealerResponse.builder()
                .id(dealerSequence.incrementAndGet())
                .city("Berlin")
                .address("Alexanderplatz 1")
                .phone("+49123456789")
                .carsCount(1)
                .build();

        dealers.put(dealer1.getId(), dealer1);
        dealers.put(dealer2.getId(), dealer2);


        // --- Cars ---
        CarResponse car1 = CarResponse.builder()
                .id(carSequence.incrementAndGet())
                .brand("BMW")
                .model("M5 F90")
                .year(2022)
                .price(120000.0)
                .quantity(5)
                .dealerId(dealer1.getId())
                .specifications(Map.of("engine", "4.4 V8", "hp", 600))
                .build();

        CarResponse car2 = CarResponse.builder()
                .id(carSequence.incrementAndGet())
                .brand("Audi")
                .model("RS7")
                .year(2023)
                .price(130000.0)
                .quantity(3)
                .dealerId(dealer2.getId())
                .specifications(Map.of("engine", "4.0 V8", "hp", 591))
                .build();

        CarResponse car3 = CarResponse.builder()
                .id(carSequence.incrementAndGet())
                .brand("Mercedes")
                .model("AMG GT")
                .year(2021)
                .price(150000.0)
                .quantity(2)
                .dealerId(dealer1.getId())
                .specifications(Map.of("engine", "4.0 V8", "hp", 630))
                .build();

        cars.put(car1.getId(), car1);
        cars.put(car2.getId(), car2);
        cars.put(car3.getId(), car3);
    }
}