package com.s20683.wmphs.order;

public enum OrderState {
    INIT(0);

    OrderState(int value) {
        this.value = value;
    }
    private final int value;

    public int getValue() {
        return value;
    }
}
