package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.service.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {


    // set the states
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        // configure the State
        states.withStates()
                .initial(PaymentState.NEW) // initial state
                .states(EnumSet.allOf(PaymentState.class)) // load all states
                .end(PaymentState.AUTH) // define termination state End happy path
                .end(PaymentState.PRE_AUTH_ERROR) // define termination state End error
                .end(PaymentState.AUTH_ERROR); // define termination state End error
    }

    //set transitions
    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal() // external configuration
                .source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                        .action(preAuthAction()) // PRE_AUTHORIZE does not change state
                        .guard(paymentGuardId()) // Validate(guard) that the payment include paymentId
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED) // If PRE_AUTH_APPROVED change state from New -> PRE_AUTH
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED); // if PRE_AUTH_DECLINED change state from New -> PRE_AUTH_DECLINED

        transitions.withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                        .action(authAction())
                        .guard(paymentGuardId()) // Validate(guard) that the payment include paymentId
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED);
    }

    // define the state Listener
    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){

            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChange(from: %s, to:%s)", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }

    // configure a guard to ensure that the PaymentID is included
    // if the paymentHeader does not include PaymentId the process will not proceed
    public Guard<PaymentState, PaymentEvent> paymentGuardId(){
        return stateContext -> {
            // return FALSE or TRUE value if the paymentId exist.
            return stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }

    // this will be called on a PRE_AUTH event
    // used to emulate Approved or Declined events
    public Action<PaymentState, PaymentEvent> preAuthAction(){

//        this is where you will implement business logic, like API call, check value in DB ect ...
        return context -> {
            System.out.println("preAuth was called !!");

            if(new Random().nextInt(10) < 8){
                System.out.println("Approved");

                // Send an event to the state machine to APPROVED the auth request
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                .build());
            } else {
                System.out.println("Declined No Credit !!");

                // Send an event to the state machine to DECLINED the auth request, and add the
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }

    public Action<PaymentState, PaymentEvent> authAction(){

//        this is where you will implement business logic, like API call, check value in DB ect ...
        return context -> {
            System.out.println("preAuth was called !!");

            if(new Random().nextInt(10) < 5){
                System.out.println("Approved");

                // Send an event to the state machine to APPROVED the auth request
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            } else {
                System.out.println("Declined No Credit !!");

                // Send an event to the state machine to DECLINED the auth request, and add the
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
}
