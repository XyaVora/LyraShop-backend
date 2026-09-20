package com.lyrashop.order.service;

public class InvalidVoucherException extends RuntimeException {
    public InvalidVoucherException() { super("Voucher is invalid or its conditions are not met"); }
}
