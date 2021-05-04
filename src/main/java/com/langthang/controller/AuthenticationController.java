package com.langthang.controller;

import com.langthang.annotation.ValidEmail;
import com.langthang.dto.ResetPasswordDTO;
import com.langthang.dto.UserDTO;
import com.langthang.event.OnRegistrationEvent;
import com.langthang.event.OnResetPasswordEvent;
import com.langthang.model.entity.Account;
import com.langthang.model.entity.RegisterToken;
import com.langthang.services.IAuthServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private IAuthServices authServices;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestParam("email") @ValidEmail String email,
            @RequestParam("password") String password,
            HttpServletResponse resp) {

        String jwtToken = authServices.signIn(email, password, resp);
        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Object> refreshToken(
            @CookieValue(name = "refresh-token", defaultValue = "")
            @NotBlank String clientToken,
            HttpServletRequest req,
            HttpServletResponse resp) {

        String newJwtToken = authServices.refreshToken(clientToken, req, resp);
        return new ResponseEntity<>(newJwtToken, HttpStatus.OK);
    }

    @PostMapping("/registration")
    public ResponseEntity<Object> register(
            @Valid UserDTO userDTO,
            HttpServletRequest req) {

        Account account = authServices.registerNewAccount(userDTO);
        eventPublisher.publishEvent(new OnRegistrationEvent(account, getAppUrl(req)));

        return new ResponseEntity<>("OK", HttpStatus.CREATED);
    }

    @GetMapping("/registrationConfirm")
    public ResponseEntity<Object> confirmRegistration(
            @RequestParam("token") String token) {
        authServices.validateRegisterToken(token);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/resendRegistrationToken")
    public ResponseEntity<Object> resendRegistrationToken(
            @RequestParam("token") String existToken,
            HttpServletRequest req) {

        RegisterToken newToken = authServices.generateNewRegisterToken(existToken);
        eventPublisher.publishEvent(new OnRegistrationEvent(newToken.getAccount(), getAppUrl(req), newToken.getToken()));

        return new ResponseEntity<>("OK", HttpStatus.CREATED);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Object> resetPassword(@RequestParam("email") String email,
                                                HttpServletRequest req) {
        Account account = authServices.findAccountByEmail(email);

        if (account == null) {
            return new ResponseEntity<>("Email not found", HttpStatus.FORBIDDEN);
        }
        eventPublisher.publishEvent(new OnResetPasswordEvent(account, getAppUrl(req)));

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/changePassword")
    public ResponseEntity<Object> verifyResetPasswordToken(@RequestParam("token") String token) {
        authServices.validatePasswordResetToken(token);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/savePassword")
    public ResponseEntity<Object> savePassword(@Valid ResetPasswordDTO resetPasswordDTO) {
        authServices.validatePasswordResetToken(resetPasswordDTO.getToken());

        Account account = authServices.findAccountByPasswordResetToken(resetPasswordDTO.getToken());
        if (account != null) {
            authServices.changeAccountPassword(account, resetPasswordDTO.getNewPassword());
            return new ResponseEntity<>("Password has changed", HttpStatus.OK);
        }

        return new ResponseEntity<>("Account not found", HttpStatus.FORBIDDEN);
    }

    /*----------------NON-API----------------*/
    private String getAppUrl(HttpServletRequest req) {
        return "http://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
    }

}
