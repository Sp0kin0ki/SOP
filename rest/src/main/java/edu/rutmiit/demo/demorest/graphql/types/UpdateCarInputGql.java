package edu.rutmiit.demo.demorest.graphql.types;

import java.util.Map;

public record UpdateCarInputGql(
        String brand,
        String model,
        Integer year,
        Double price,
        Integer quantity,
        Long dealerId,
        Map<String, Object> specifications
) {}