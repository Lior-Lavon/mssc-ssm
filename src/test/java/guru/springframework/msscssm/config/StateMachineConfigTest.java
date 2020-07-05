package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewTestMachine(){
        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());

        // set init State
        sm.start();
        log.debug(sm.getState().toString());

        // send event PRE_AUTHORIZE , should stay on NEW
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        log.debug(sm.getState().toString());

        // send event PRE_AUTH_APPROVED , should change to PRE_AUTH
        sm.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        log.debug(sm.getState().toString());

    }

}