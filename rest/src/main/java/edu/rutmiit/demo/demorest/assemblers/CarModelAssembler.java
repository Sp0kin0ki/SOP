package edu.rutmiit.demo.demorest.assemblers;

import edu.rutmiit.demo.carsapicontract.dto.CarResponse;
import edu.rutmiit.demo.demorest.controllers.CarController;
import edu.rutmiit.demo.demorest.controllers.DealerController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CarModelAssembler implements RepresentationModelAssembler<CarResponse, EntityModel<CarResponse>> {

    @Override
    public EntityModel<CarResponse> toModel(CarResponse car) {

        return EntityModel.of(car,
                linkTo(methodOn(CarController.class).getCarById(car.getId())).withSelfRel(),

                linkTo(methodOn(CarController.class)
                        .getAllCars(null, null, null, null, null, null, 0, 20))
                        .withRel("cars"),

                linkTo(methodOn(DealerController.class)
                        .getDealerById(car.getDealerId()))
                        .withRel("dealer")
        );
    }
}