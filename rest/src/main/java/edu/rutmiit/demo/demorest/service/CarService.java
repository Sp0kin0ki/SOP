package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.carsapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.demorest.event.CarEventPublisher;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final InMemoryStorage storage;
    private final CarEventPublisher eventPublisher;

    public CarService(
        InMemoryStorage storage,
        CarEventPublisher eventPublisher
    ) {
        this.storage = storage;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<CarResponse> findAll(
            Long dealerId,
            String brand,
            String model,
            Integer year,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        List<CarResponse> all = storage.cars.values().stream()
                .filter(c -> dealerId == null || c.getDealerId().equals(dealerId))
                .filter(c -> brand == null || c.getBrand().equalsIgnoreCase(brand))
                .filter(c -> model == null || c.getModel().equalsIgnoreCase(model))
                .filter(c -> year == null || c.getYear().equals(year))
                .filter(c -> minPrice == null || c.getPrice() >= minPrice)
                .filter(c -> maxPrice == null || c.getPrice() <= maxPrice)
                .sorted(Comparator.comparingLong(CarResponse::getId))
                .toList();

        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;

        int from = page * size;
        int to = Math.min(from + size, totalElements);

        List<CarResponse> content =
                (from >= totalElements) ? List.of() : all.subList(from, to);

        return new PagedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page >= totalPages - 1
        );
    }

    public CarResponse findById(Long id) {
        return Optional.ofNullable(storage.cars.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
    }

    public CarResponse create(CarRequest request) {

        long id = storage.carSequence.incrementAndGet();

        CarResponse car = CarResponse.builder()
                .id(id)
                .brand(request.brand())
                .model(request.model())
                .year(request.year())
                .price(request.price())
                .quantity(request.quantity())
                .dealerId(request.dealerId())
                .specifications(request.specifications())
                .build();

        storage.cars.put(id, car);

        eventPublisher.publishCreated(car);
        return car;
    }

    public CarResponse update(Long id, UpdateCarRequest request) {

        findById(id);

        CarResponse updated = CarResponse.builder()
                .id(id)
                .brand(request.brand())
                .model(request.model())
                .year(request.year())
                .price(request.price())
                .quantity(request.quantity())
                .dealerId(request.dealerId())
                .specifications(request.specifications())
                .build();

        storage.cars.put(id, updated);

        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public CarResponse patch(Long id, PatchCarRequest request) {

        CarResponse existing = findById(id);

        CarResponse updated = CarResponse.builder()
                .id(id)
                .brand(request.brand() != null ? request.brand() : existing.getBrand())
                .model(request.model() != null ? request.model() : existing.getModel())
                .year(request.year() != null ? request.year() : existing.getYear())
                .price(request.price() != null ? request.price() : existing.getPrice())
                .quantity(request.quantity() != null ? request.quantity() : existing.getQuantity())
                .dealerId(request.dealerId() != null ? request.dealerId() : existing.getDealerId())
                .specifications(request.specifications() != null ? request.specifications() : existing.getSpecifications())
                .build();

        storage.cars.put(id, updated);

        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        String brand = storage.cars.get(id).getBrand();
        String model = storage.cars.get(id).getModel();
        storage.cars.remove(id);
        eventPublisher.publishDeleted(id, brand, model);
    }
}