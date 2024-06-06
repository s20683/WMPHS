package com.s20683.wmphs.order;

public enum OrderState {
    INIT(0), RELEASED(1), COMPLETATION(2), COMPLETED(3);

    OrderState(int value) {
        this.value = value;
    }
    private final int value;

    public int getValue() {
        return value;
    }
}
