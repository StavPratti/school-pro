package gr.aueb.cf.schoolapp.authentication;

import gr.aueb.cf.schoolapp.dto.UserLoginDTO;
import gr.aueb.cf.schoolapp.service.IUserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class AuthenticationProvider {

    private final IUserService userService;

    public boolean authenticate(UserLoginDTO userLoginDTO) {
        return userService.isUserValid(userLoginDTO.getUsername(), userLoginDTO.getPassword());
    }
}


