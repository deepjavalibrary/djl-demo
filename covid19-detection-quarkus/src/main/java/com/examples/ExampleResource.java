package com.examples;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/predict")
public class ExampleResource {

    @Inject
    ExampleService exampleService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String predict() throws Exception {
        return exampleService.predict().toString();
    }
}