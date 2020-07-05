package guru.springframework.msscssm.domain;

public enum PaymentEvent {

    PRE_AUTHORIZE, // From New state -> To New state
    PRE_AUTH_APPROVED, // From New state -> To PRE_AUTH state
    PRE_AUTH_DECLINED, // From New state -> To PRE_AUTH_ERROR state
    AUTHORIZE,
    AUTH_APPROVED,
    AUTH_DECLINED
}
