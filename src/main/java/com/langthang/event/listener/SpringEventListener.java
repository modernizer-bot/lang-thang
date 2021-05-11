package com.langthang.event.listener;

import com.langthang.event.OnNewNotifyEvent;
import com.langthang.event.OnRegisterWithGoogle;
import com.langthang.event.OnRegistrationEvent;
import com.langthang.event.OnResetPasswordEvent;
import com.langthang.model.entity.Account;
import com.langthang.services.IAuthServices;
import com.langthang.utils.MyMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringEventListener {

    @Autowired
    private IAuthServices authServices;

    @Autowired
    private MyMailSender mailSender;

//    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${application.broker.notify.dest}")
    private String broadcastNotifyEndpoint;

    @EventListener
    @Async
    public void handleConfirmRegistration(OnRegistrationEvent event) {
        Account account = event.getAccount();
        String token = event.getToken();

        if (token == null) {
            token = authServices.createVerifyToken(account);
        }

        mailSender.sendRegisterTokenEmail(event.getAppUrl()
                , token
                , account);
    }

    @EventListener
    @Async
    public void handleResetPassword(OnResetPasswordEvent event) {
        Account account = event.getAccount();
        String token = authServices.createPasswordResetToken(account);

        mailSender.sendResetPasswordEmail(event.getAppUrl()
                , token
                , account);
    }

    @EventListener
    @Async
    public void handleRegisterWithGoogle(OnRegisterWithGoogle event) {
        Account account = event.getAccount();
        String rawPassword = event.getRawPassword();

        mailSender.sendCreatedAccountEmail(account, rawPassword);
    }

    @EventListener
    @Async
    public void handleNewNotify(OnNewNotifyEvent event) {
        messagingTemplate.convertAndSend(broadcastNotifyEndpoint, event);
    }
}
