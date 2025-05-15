package gr.aueb.cf.schoolapp.core.services;

import gr.aueb.cf.schoolapp.dto.ResponseMessageDTO;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class AppNotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {

        Response.Status status = Response.Status.UNAUTHORIZED;

        return Response.status(status)
                .entity(new ResponseMessageDTO("UserNotAuthorized", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
