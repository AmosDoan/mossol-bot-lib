package net.mossol.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.centraldogma.client.CentralDogma;

abstract class ContextConfiguration {
    static final CentralDogma centralDogma = CentralDogma.forHost("mossol.net");
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static final String CENTRAL_DOGMA_PROJECT = "mossol";
    static final String CENTRAL_DOGMA_REPOSITORY = "main";
}
