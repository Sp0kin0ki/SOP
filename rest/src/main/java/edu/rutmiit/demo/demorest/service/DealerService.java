package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.carsapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.demorest.event.DealerEventPublisher;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DealerService {

    private final InMemoryStorage storage;
    private final DealerEventPublisher eventPublisher;

    public DealerService(
        InMemoryStorage storage, 
        DealerEventPublisher eventPublisher
    ) {
        this.storage = storage;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<DealerResponse> findAll(String city, int page, int size) {

        List<DealerResponse> all = storage.dealers.values().stream()
                .filter(d -> city == null || d.getCity().equalsIgnoreCase(city))
                .sorted(Comparator.comparingLong(DealerResponse::getId))
                .toList();

        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;

        int from = page * size;
        int to = Math.min(from + size, totalElements);

        List<DealerResponse> content =
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

    public DealerResponse findById(Long id) {
        return Optional.ofNullable(storage.dealers.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", id));
    }

    public DealerResponse create(DealerRequest request) {

        long id = storage.dealerSequence.incrementAndGet();

        DealerResponse dealer = DealerResponse.builder()
                .id(id)
                .city(request.city())
                .address(request.address())
                .phone(request.phone())
                .carsCount(0)
                .build();

        storage.dealers.put(id, dealer);

        eventPublisher.publishCreated(dealer);
        return dealer;
    }

    public DealerResponse update(Long id, UpdateDealerRequest request) {

        DealerResponse existing = findById(id);

        DealerResponse updated = DealerResponse.builder()
                .id(id)
                .city(request.city())
                .address(request.address())
                .phone(request.phone())
                .carsCount(existing.getCarsCount())
                .build();

        storage.dealers.put(id, updated);

        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public DealerResponse patch(Long id, PatchDealerRequest request) {

        DealerResponse existing = findById(id);

        DealerResponse updated = DealerResponse.builder()
                .id(id)
                .city(request.city() != null ? request.city() : existing.getCity())
                .address(request.address() != null ? request.address() : existing.getAddress())
                .phone(request.phone() != null ? request.phone() : existing.getPhone())
                .carsCount(existing.getCarsCount())
                .build();

        storage.dealers.put(id, updated);

        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        String city = storage.dealers.get(id).getCity();
        Integer carsCount = storage.dealers.get(id).getCarsCount();
        storage.dealers.remove(id);
        eventPublisher.publishDeleted(id, city, carsCount);
    }

    public PagedResponse<CarResponse> findAllCarsByDealer(Long dealerId, int page, int size) {

        findById(dealerId);

        List<CarResponse> all = storage.cars.values().stream()
                .filter(c -> c.getDealerId().equals(dealerId))
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
}