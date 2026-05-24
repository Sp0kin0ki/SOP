package edu.rutmiit.demo.demorest.assemblers;

import edu.rutmiit.demo.carsapicontract.dto.DealerResponse;
import edu.rutmiit.demo.demorest.controllers.DealerController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DealerModelAssembler implements RepresentationModelAssembler<DealerResponse, EntityModel<DealerResponse>> {

    @Override
    public EntityModel<DealerResponse> toModel(DealerResponse dealer) {

        return EntityModel.of(dealer,
                linkTo(methodOn(DealerController.class)
                        .getDealerById(dealer.getId()))
                        .withSelfRel(),

                linkTo(methodOn(DealerController.class)
                        .getAllDealers(null, 0, 20))
                        .withRel("dealers"),

                linkTo(methodOn(DealerController.class)
                        .getAllCars(dealer.getId(), 0, 20))
                        .withRel("cars")
        );
    }
}