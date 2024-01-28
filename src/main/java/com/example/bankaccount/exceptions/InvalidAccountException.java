package com.example.bankaccount.exceptions;

public class InvalidAccountException extends AccountException {
    public InvalidAccountException(String message) {
        super(message);
    }
}