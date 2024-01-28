package com.example.bankaccount.service;

import com.example.bankaccount.exceptions.AccountException;
import com.example.bankaccount.exceptions.InsufficientFundsException;
import com.example.bankaccount.exceptions.InvalidAccountException;
import com.example.bankaccount.exceptions.InvalidTransferException;
import com.example.bankaccount.model.Account;
import com.example.bankaccount.repo.AccountRepo;

import io.micrometer.common.util.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepo accountRepo;

    public AccountServiceImpl(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public BigDecimal getBalance(String accountNumber) throws InvalidAccountException {
        Account account = accountRepo.findById(accountNumber)
                .orElseThrow(() -> new InvalidAccountException("Account not found"));
        return account.getBalance();
    }

    @Override
    public Account createAccount(Account account) throws AccountException {
        if (account == null) {
            throw new AccountException("Account data cannot be null");
        }

        if (StringUtils.isEmpty(account.getAccountNumber()) || StringUtils.isEmpty(account.getFirstName()) || StringUtils.isEmpty(account.getLastName())) {
            throw new AccountException("Invalid account data");
        }

        Optional<Account> existingAccount = accountRepo.findById(account.getAccountNumber());
        if (existingAccount.isPresent()) {
            throw new AccountException("An account with the same account number already exists");
        }
        return accountRepo.save(account);
    }

    @Override
    public BigDecimal deposit(String accountNumber, BigDecimal amount) throws InvalidAccountException, InvalidTransferException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Amount must be a positive value");
        }

        Account account = accountRepo.findById(accountNumber)
                .orElseThrow(() -> new InvalidAccountException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        return account.getBalance();
    }

    @Override
    @Transactional
    public BigDecimal transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount)
            throws InvalidAccountException, InsufficientFundsException, InvalidTransferException {
        // Validate input
        validateTransfer(sourceAccountNumber, destinationAccountNumber, amount);

        // Retrieve accounts
        Account sourceAccount = accountRepo.findById(sourceAccountNumber)
                .orElseThrow(() -> new InvalidAccountException("Source account not found"));
        Account destinationAccount = accountRepo.findById(destinationAccountNumber)
                .orElseThrow(() -> new InvalidAccountException("Destination account not found"));

        // Check for sufficient funds in the source account
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in the source account");
        }

        // Perform the transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        // Save the updated accounts
        accountRepo.save(sourceAccount);
        accountRepo.save(destinationAccount);

        // Return the new balance of the source account
        return sourceAccount.getBalance();
    }

    private void validateTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Amount must be a positive value");
        }
        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new InvalidTransferException("Not possible to transfer money to the same account");
        }
    }
}