package com.meteordevelopments.duels.util.validator;

public interface TriValidator<T1, T2, T3> {
    
    boolean shouldValidate();

    boolean validate(final T1 first, final T2 second, final T3 third);
}
