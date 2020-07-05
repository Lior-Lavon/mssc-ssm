package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    // set the StateChange Listener and intercept the event change

    @Override
    public void preStateChange(State<PaymentState,
                               PaymentEvent> state,
                               Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {

        // befor estate change , If a message is present then get the paymentId from the header.
        Optional.ofNullable(message)
                .ifPresent(msg -> {
                    Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                            .ifPresent(paymentId -> {
                                // get the payment from the DB based on the paymentId that exist in the header
                                Payment payment = paymentRepository.getOne(paymentId);

                                // set the state from the pass payment state in value
                                payment.setState(state.getId());

                                // save back the payment
                                paymentRepository.save(payment);
                            });
                });

    }
}
