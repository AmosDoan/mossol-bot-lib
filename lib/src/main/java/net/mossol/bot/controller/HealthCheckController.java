package net.mossol.bot.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.Default;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

@Service
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private static final String template = "%dth, Hello, %s!";

    private final AtomicLong counter = new AtomicLong();

    @Get("/healthCheck")
    public HttpResponse healthCheck(@Param("name") @Default("world") String name) {
        logger.debug("health check");
        return HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8,
                               String.format(template, counter.incrementAndGet(), name));
    }
}
