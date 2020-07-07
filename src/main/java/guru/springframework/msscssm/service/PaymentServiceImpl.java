package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Data
@Builder
@Service
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> smFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    public PaymentServiceImpl(PaymentRepository paymentRepository, StateMachineFactory<PaymentState, PaymentEvent> smFactory, PaymentStateChangeInterceptor paymentStateChangeInterceptor) {
        this.paymentRepository = paymentRepository;
        this.smFactory = smFactory;
        this.paymentStateChangeInterceptor = paymentStateChangeInterceptor;
    }

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        // get the statemachine from the Database
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        // send message to statemachine with paymentId and PRE_AUTHORIZE
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> auth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    // Send a message to the StateMachine , add the paymentId in the header,
    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){

        // create the message and add the paymentId to the header
        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        // send the message
        sm.sendEvent(msg);
    }

    // Private function ->
    // load a stateMachine from the DB and restate its state from the DB value
    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){

        // get the payment from the DB by ID
        Payment payment = paymentRepository.getOne(paymentId);

        // restore the state machine
        StateMachine<PaymentState, PaymentEvent> sm = smFactory.getStateMachine(Long.toString(payment.getId()));

        // stop the statemachine
        sm.stop();

        // set stateMachine to the specific state of the payment from the database
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma ->{

                    // add an interceptor to the StateMachineAccessor
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);

                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        // start the statemachine
        sm.start();

        return sm;
    }
}
