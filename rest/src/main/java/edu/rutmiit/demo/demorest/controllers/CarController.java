package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.carsapicontract.endpoints.CarApi;
import edu.rutmiit.demo.demorest.assemblers.CarModelAssembler;
import edu.rutmiit.demo.demorest.service.CarService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CarController implements CarApi {

    private final CarService carService;
    private final CarModelAssembler carModelAssembler;
    private final PagedResourcesAssembler<CarResponse> pagedAssembler;

    public CarController(CarService carService,
                         CarModelAssembler carModelAssembler,
                         PagedResourcesAssembler<CarResponse> pagedAssembler) {
        this.carService = carService;
        this.carModelAssembler = carModelAssembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Override
    public PagedModel<EntityModel<CarResponse>> getAllCars(
            Long dealerId,
            String brand,
            String model,
            Integer year,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        PagedResponse<CarResponse> paged = carService.findAll(
                dealerId, brand, model, year, minPrice, maxPrice, page, size
        );

        Page<CarResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );

        return pagedAssembler.toModel(springPage, carModelAssembler);
    }

    @Override
    public EntityModel<CarResponse> getCarById(Long id) {
        return carModelAssembler.toModel(carService.findById(id));
    }

    @Override
    public ResponseEntity<EntityModel<CarResponse>> createBook(@Valid CarRequest request) {
        CarResponse created = carService.create(request);
        EntityModel<CarResponse> model = carModelAssembler.toModel(created);

        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<CarResponse> updateCar(Long id, @Valid UpdateCarRequest request) {
        return carModelAssembler.toModel(carService.update(id, request));
    }

    @Override
    public EntityModel<CarResponse> patchCar(Long id, @Valid PatchCarRequest request) {
        return carModelAssembler.toModel(carService.patch(id, request));
    }

    @Override
    public void deleteCar(Long id) {
        carService.delete(id);
    }
}