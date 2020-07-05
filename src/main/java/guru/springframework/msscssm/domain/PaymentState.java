package guru.springframework.msscssm.domain;

//State Machine Payment state
public enum PaymentState {
    NEW,
    PRE_AUTH,
    PRE_AUTH_ERROR,
    AUTH,
    AUTH_ERROR
}

// New -> PRE_AUTH -> AUTH
// New -> PRE_AUTH -> PRE_AUTH_ERROR
// NEW -> PRE_AUTH -> AUTH_ERROR