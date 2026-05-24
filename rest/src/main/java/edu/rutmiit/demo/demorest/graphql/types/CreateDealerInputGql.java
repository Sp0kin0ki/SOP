package edu.rutmiit.demo.demorest.graphql.types;

public record CreateDealerInputGql(
        String city,
        String address,
        String phone
) {}