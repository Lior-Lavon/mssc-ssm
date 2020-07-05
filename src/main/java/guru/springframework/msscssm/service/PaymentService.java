package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    // create a new Payment
    Payment newPayment(Payment payment);

    // Do Pre Auth
    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    // Do Authorize Approved
    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    // Do Decline Auth
    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);


}
