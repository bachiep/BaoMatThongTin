package com.bachiep.sems.exception;

public class AccountTemporarilyLockedException extends RuntimeException {
    public AccountTemporarilyLockedException(String message) {
        super(message);
    }
}
