package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        int status = 500; // Default to Internal Server Error
        String message = "An unexpected error occurred on the smart campus server.";

        if (exception instanceof RoomNotEmptyException) {
            status = 409;
        } else if (exception instanceof LinkedResourceNotFoundException) {
            status = 422;
        } else if (exception instanceof SensorUnavailableException) {
            status = 403;
        } else if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        } else {
            message = "An unexpected error occurred on the smart campus server.";
        }
        ErrorMessage errorEntity = new ErrorMessage(message, status);

        return Response.status(status)
                .entity(errorEntity)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}