package com.example.bankaccount.exceptions;

public class InvalidTransferException extends AccountException {
    public InvalidTransferException(String message) {
        super(message);
    }
}