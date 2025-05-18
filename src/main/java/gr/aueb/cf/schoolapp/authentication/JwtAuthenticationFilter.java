package gr.aueb.cf.schoolapp.authentication;

import gr.aueb.cf.schoolapp.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.schoolapp.dao.IUserDAO;
import gr.aueb.cf.schoolapp.model.User;
import gr.aueb.cf.schoolapp.security.CustomSecurityContext;
import gr.aueb.cf.schoolapp.security.JwtService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

@Provider
@Priority(Priorities.AUTHENTICATION)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtService jwtService;
    private final IUserDAO userDAO;

    @Context
    SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        UriInfo uriInfo = requestContext.getUriInfo();

        // if we have /api/auth/register
        // getPath() will return auth/register
        String path = uriInfo.getPath();
        if (isPublicPath(path)) {
            return;
        }
//        try {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            LOGGER.warn("Missing or invalid Authorization header");
            throw new NotAuthorizedException("User", "Authorization header must be provided");
//                throw new EntityNotAuthorizedException("User", "Authorization header must be provided");
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            String username = jwtService.extractSubject(token);
            if (username != null && securityContext == null || securityContext.getUserPrincipal() == null) {
                User user = userDAO.getByUsername(username).orElse(null);
                if (user != null && jwtService.isTokenValid(token, user)) {
                    requestContext.setSecurityContext(new CustomSecurityContext(user));
                } else {
                    //System.out.println("Token is not valid" + requestContext.getUriInfo());
                    throw new NotAuthorizedException("User", "User or Token not valid");
                }
            }
//            String username = jwtService.extractSubject(token);
//            if (username == null) {
//                LOGGER.warn("Invalid token - no subject");
//                throw new EntityNotAuthorizedException("User", "Invalid token");
//            }
//
//            if (securityContext == null || securityContext.getUserPrincipal() == null) {
//                User user = userDAO.getByUsername(username)
//                        .orElseThrow(() -> {
//                            LOGGER.warn("User not found {}", username);
//                            return new EntityNotAuthorizedException("User", "Invalid credentials");
//                        });
//
//                if (!jwtService.isTokenValid(token, user)) {
//                    LOGGER.warn("Invalid token for user {}",  username);
//                    throw new EntityNotAuthorizedException("User", "Invalid token");
//                }
//
//                requestContext.setSecurityContext(new CustomSecurityContext(user));
//            }
        } catch (Exception e) {
            LOGGER.error("JWT validation failed", e);
        }
    }

    private boolean isPublicPath(String path) {
        return path.equals("auth/register") || path.equals("auth/login");
    }
}