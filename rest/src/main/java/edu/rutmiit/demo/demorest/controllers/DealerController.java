package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.carsapicontract.endpoints.DealerApi;
import edu.rutmiit.demo.demorest.assemblers.CarModelAssembler;
import edu.rutmiit.demo.demorest.assemblers.DealerModelAssembler;
import edu.rutmiit.demo.demorest.service.DealerService;

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
public class DealerController implements DealerApi {

    private final DealerService dealerService;
    private final DealerModelAssembler dealerModelAssembler;
    private final CarModelAssembler carModelAssembler;

    private final PagedResourcesAssembler<DealerResponse> pagedDealersAssembler;
    private final PagedResourcesAssembler<CarResponse> pagedCarsAssembler;

    public DealerController(DealerService dealerService,
                            DealerModelAssembler dealerModelAssembler,
                            CarModelAssembler carModelAssembler,
                            PagedResourcesAssembler<DealerResponse> pagedDealersAssembler,
                            PagedResourcesAssembler<CarResponse> pagedCarsAssembler) {
        this.dealerService = dealerService;
        this.dealerModelAssembler = dealerModelAssembler;
        this.carModelAssembler = carModelAssembler;
        this.pagedDealersAssembler = pagedDealersAssembler;
        this.pagedCarsAssembler = pagedCarsAssembler;
    }

    @Override
    public EntityModel<DealerResponse> getDealerById(Long id) {
        return dealerModelAssembler.toModel(dealerService.findById(id));
    }

    @Override
    public PagedModel<EntityModel<DealerResponse>> getAllDealers(String city, int page, int size) {

        PagedResponse<DealerResponse> paged =
                dealerService.findAll(city, page, size);

        Page<DealerResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );

        return pagedDealersAssembler.toModel(springPage, dealerModelAssembler);
    }

    @Override
    public PagedModel<EntityModel<CarResponse>> getAllCars(long id, int page, int size) {

        PagedResponse<CarResponse> paged =
                dealerService.findAllCarsByDealer(id, page, size);

        Page<CarResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );

        return pagedCarsAssembler.toModel(springPage, carModelAssembler);
    }

    @Override
    public ResponseEntity<EntityModel<DealerResponse>> createDealer(@Valid DealerRequest request) {

        DealerResponse created = dealerService.create(request);
        EntityModel<DealerResponse> model = dealerModelAssembler.toModel(created);

        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<DealerResponse> updateDealer(Long id, @Valid UpdateDealerRequest request) {
        return dealerModelAssembler.toModel(dealerService.update(id, request));
    }

    @Override
    public EntityModel<DealerResponse> patchDealer(Long id, @Valid PatchDealerRequest request) {
        return dealerModelAssembler.toModel(dealerService.patch(id, request));
    }

    @Override
    public void deleteDealer(Long id) {
        dealerService.delete(id);
    }
}