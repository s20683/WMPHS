package com.s20683.wmphs.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result<T> {

    private boolean success;
    private T value;
    private Exception exception;

    public static <K> Result<K> createSuccess(K value) {
        return new Result<>(true, value, null);
    }
    public static <K> Result<K> createFail(Exception exception) {
        return new Result<>(false, null, exception);
    }
}
