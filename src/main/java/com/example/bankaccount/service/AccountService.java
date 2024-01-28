package com.example.bankaccount.service;

import java.math.BigDecimal;

import com.example.bankaccount.exceptions.AccountException;
import com.example.bankaccount.exceptions.InsufficientFundsException;
import com.example.bankaccount.exceptions.InvalidAccountException;
import com.example.bankaccount.exceptions.InvalidTransferException;
import com.example.bankaccount.model.Account;

public interface AccountService {
    BigDecimal transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount)
            throws InvalidAccountException, InsufficientFundsException, InvalidTransferException;

    BigDecimal getBalance(String accountNumber) throws AccountException;

    Account createAccount(Account account) throws AccountException;

    BigDecimal deposit(String accountNumber, BigDecimal amount) throws InvalidAccountException, InvalidTransferException;
}