package com.s20683.plc;

@FunctionalInterface
public interface MokaOperation {
    public void handle(int id, int size, byte[] value);
}
