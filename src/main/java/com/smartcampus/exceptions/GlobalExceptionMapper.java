/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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