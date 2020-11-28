package net.mossol.bot.connection;

import java.lang.reflect.ParameterizedType;

import javax.annotation.Nullable;

import net.mossol.bot.model.LocationInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.RequestConverterFunction;

public class LocationInfoRequestConverter implements RequestConverterFunction {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Nullable
    @Override
    public Object convertRequest(ServiceRequestContext ctx, AggregatedHttpRequest request,
                                 Class<?> expectedResultType,
                                 @Nullable ParameterizedType expectedParameterizedResultType) throws Exception {
        if (expectedResultType == LocationInfo.class) {
            return objectMapper.readValue(request.contentUtf8(), LocationInfo.class);
        }

        return RequestConverterFunction.fallthrough();    }
}

