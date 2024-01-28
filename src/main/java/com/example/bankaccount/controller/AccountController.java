package com.example.bankaccount.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.bankaccount.exceptions.AccountException;
import com.example.bankaccount.exceptions.InsufficientFundsException;
import com.example.bankaccount.exceptions.InvalidAccountException;
import com.example.bankaccount.exceptions.InvalidTransferException;
import com.example.bankaccount.model.Account;
import com.example.bankaccount.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        try {
            BigDecimal balance = accountService.getBalance(accountNumber);
            return ResponseEntity.ok("Balance: " + balance);
        } catch (InvalidAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account accountData) {
        try {
            Account savedAccount = accountService.createAccount(accountData);
            return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{accountNumber}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String accountNumber, @RequestParam(name = "amount") BigDecimal amount) {
        try {
            BigDecimal newBalance = accountService.deposit(accountNumber, amount);
            return ResponseEntity.ok("New balance: " + newBalance);
        } catch (InvalidAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidTransferException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MethodArgumentTypeMismatchException e) {
            return ResponseEntity.badRequest().body("Invalid amount format");
        }
    }

    @PutMapping("/{sourceAccountNumber}/transfer")
    public ResponseEntity<?> transfer(@PathVariable String sourceAccountNumber,
                                      @RequestParam("destinationAccountNumber") String destinationAccountNumber,
                                      @RequestParam("amount") BigDecimal amount) {
        try {
            BigDecimal newBalance = accountService.transfer(sourceAccountNumber, destinationAccountNumber, amount);
            return ResponseEntity.ok("Transfer successful. New balance of source account: " + newBalance);
        } catch (InvalidAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InvalidTransferException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
