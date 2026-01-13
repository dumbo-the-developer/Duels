package com.meteordevelopments.duels.util.validator;

import com.google.common.collect.ImmutableList;

public final class ValidatorUtil {
    
    @SafeVarargs
    public static <T> ImmutableList<Validator<T>> buildList(final Validator<T>... validators) {
        final ImmutableList.Builder<Validator<T>> builder = ImmutableList.builder();

        for (final Validator<T> validator : validators) {
            if (validator.shouldValidate()) {
                builder.add(validator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T1, T2> ImmutableList<BiValidator<T1, T2>> buildList(final BiValidator<T1, T2>... biValidators) {
        final ImmutableList.Builder<BiValidator<T1, T2>> builder = ImmutableList.builder();

        for (final BiValidator<T1, T2> biValidator : biValidators) {
            if (biValidator.shouldValidate()) {
                builder.add(biValidator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T1, T2, T3> ImmutableList<TriValidator<T1, T2, T3>> buildList(final TriValidator<T1, T2, T3>... triValidators) {
        final ImmutableList.Builder<TriValidator<T1, T2, T3>> builder = ImmutableList.builder();

        for (final TriValidator<T1, T2, T3> triValidator : triValidators) {
            if (triValidator.shouldValidate()) {
                builder.add(triValidator);
            }
        }

        return builder.build();
    }

    public static <T> boolean validate(final ImmutableList<Validator<T>> chain, final T validated) {
        return chain.stream().allMatch(validator -> validator.validate(validated));
    }

    public static <T1, T2> boolean validate(final ImmutableList<BiValidator<T1, T2>> chain, final T1 first, final T2 second) {
        return chain.stream().allMatch(validator -> validator.validate(first, second));
    }

    public static <T1, T2, T3> boolean validate(final ImmutableList<TriValidator<T1, T2, T3>> chain, final T1 first, final T2 second, final T3 third) {
        return chain.stream().allMatch(validator -> validator.validate(first, second, third));
    }

    private ValidatorUtil() {}
}
