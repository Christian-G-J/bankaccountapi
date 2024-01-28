package com.example.bankaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.bankaccount.exceptions.AccountException;
import com.example.bankaccount.exceptions.InsufficientFundsException;
import com.example.bankaccount.exceptions.InvalidAccountException;
import com.example.bankaccount.exceptions.InvalidTransferException;
import com.example.bankaccount.model.Account;
import com.example.bankaccount.repo.AccountRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.math.BigDecimal;

class AccountServiceImplTest {

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBalanceSuccess() {
        // Given
        String accountNumber = "123";
        BigDecimal expectedBalance = new BigDecimal("200.00");
        Account account = new Account(accountNumber, expectedBalance, "John", "Doe");

        when(accountRepo.findById(accountNumber)).thenReturn(Optional.of(account));

        // When
        BigDecimal actualBalance = accountService.getBalance(accountNumber);

        // Then
        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void testGetBalanceAccountNotFound() {
        // Given
        String accountNumber = "nonexistent";

        when(accountRepo.findById(accountNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidAccountException.class, () -> accountService.getBalance(accountNumber));
    }

    @Test
    void testGetBalanceWithNullAccountNumber() {
        // When & Then
        assertThrows(InvalidAccountException.class, () -> accountService.getBalance(null));
    }

    @Test
    void testGetBalanceWithEmptyAccountNumber() {
        // Given
        String accountNumber = "";

        // When & Then
        assertThrows(InvalidAccountException.class, () -> accountService.getBalance(accountNumber));
    }

    @Test
    void testCreateAccountSuccess() {
        // Given
        Account newAccount = new Account("123", new BigDecimal("100.00"), "John", "Doe");
        when(accountRepo.save(any(Account.class))).thenReturn(newAccount);

        // When
        Account createdAccount = accountService.createAccount(newAccount);

        // Then
        assertNotNull(createdAccount);
        assertEquals("123", createdAccount.getAccountNumber());
        assertEquals(new BigDecimal("100.00"), createdAccount.getBalance());
        assertEquals("John", createdAccount.getFirstName());
        assertEquals("Doe", createdAccount.getLastName());
    }

    @Test
    void testCreateAccountWithNullData() {
        // When & Then
        assertThrows(AccountException.class, () -> accountService.createAccount(null));
    }

    @Test
    void testCreateAccountWithInvalidData() {
        // Given
        Account invalidAccount = new Account("", new BigDecimal("100.00"), "", ""); // Invalid data

        // When & Then
        assertThrows(AccountException.class, () -> accountService.createAccount(invalidAccount));
    }

    @Test
    void testCreateAccountWithExistingAccountNumber() {
        // Given
        Account existingAccount = new Account("123", new BigDecimal("200.00"), "Jane", "Roe");
        when(accountRepo.findById("123")).thenReturn(Optional.of(existingAccount));

        Account newAccount = new Account("123", new BigDecimal("100.00"), "John", "Doe");

        // When & Then
        assertThrows(AccountException.class, () -> accountService.createAccount(newAccount));
    }

    @Test
    void testSuccessfulTransfer() throws Exception {
        // Given
        String sourceAccountNumber = "123";
        String destinationAccountNumber = "456";
        BigDecimal transferAmount = new BigDecimal("100.00");
        BigDecimal sourceInitialBalance = new BigDecimal("200.00");
        BigDecimal destinationInitialBalance = new BigDecimal("150.00");

        Account sourceAccount = new Account(sourceAccountNumber, sourceInitialBalance, "John", "Doe");
        Account destinationAccount = new Account(destinationAccountNumber, destinationInitialBalance, "Jane", "Roe");

        when(accountRepo.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(destinationAccountNumber)).thenReturn(Optional.of(destinationAccount));

        // When
        BigDecimal newSourceBalance = accountService.transfer(sourceAccountNumber, destinationAccountNumber, transferAmount);

        // Then
        assertEquals(sourceInitialBalance.subtract(transferAmount), newSourceBalance);
        assertEquals(destinationInitialBalance.add(transferAmount), destinationAccount.getBalance());

        verify(accountRepo, times(1)).save(sourceAccount);
        verify(accountRepo, times(1)).save(destinationAccount);
    }

    @Test
    void testTransferFailsWithInsufficientFunds() {
        // Given
        String sourceAccountNumber = "123";
        String destinationAccountNumber = "456";
        BigDecimal transferAmount = new BigDecimal("300.00"); // More than what's in the source account
        BigDecimal sourceInitialBalance = new BigDecimal("200.00");
        BigDecimal destinationInitialBalance = new BigDecimal("150.00");

        Account sourceAccount = new Account(sourceAccountNumber, sourceInitialBalance, "John", "Doe");
        Account destinationAccount = new Account(destinationAccountNumber, destinationInitialBalance, "Jane", "Roe");

        when(accountRepo.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(destinationAccountNumber)).thenReturn(Optional.of(destinationAccount));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> 
            accountService.transfer(sourceAccountNumber, destinationAccountNumber, transferAmount)
        );
    }

    @Test
    void testTransferFailsWithInvalidSourceAccount() {
        // Given
        String sourceAccountNumber = "123";
        String destinationAccountNumber = "456";
        BigDecimal transferAmount = new BigDecimal("50.00");

        when(accountRepo.findById(sourceAccountNumber)).thenReturn(Optional.empty()); // Source account not found

        // When & Then
        assertThrows(InvalidAccountException.class, () -> 
            accountService.transfer(sourceAccountNumber, destinationAccountNumber, transferAmount)
        );
    }

    @Test
    void testTransferFailsWithInvalidDestinationAccount() {
        // Given
        String sourceAccountNumber = "123";
        String destinationAccountNumber = "456";
        BigDecimal transferAmount = new BigDecimal("50.00");
        BigDecimal sourceInitialBalance = new BigDecimal("200.00");

        Account sourceAccount = new Account(sourceAccountNumber, sourceInitialBalance, "John", "Doe");

        when(accountRepo.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(destinationAccountNumber)).thenReturn(Optional.empty()); // Destination account not found

        // When & Then
        assertThrows(InvalidAccountException.class, () -> 
            accountService.transfer(sourceAccountNumber, destinationAccountNumber, transferAmount)
        );
    }

    @Test
    void testTransferToSelfFails() {
        // Given
        String accountNumber = "123";
        BigDecimal transferAmount = new BigDecimal("50.00");
        BigDecimal initialBalance = new BigDecimal("200.00");

        Account account = new Account(accountNumber, initialBalance, "John", "Doe");

        when(accountRepo.findById(accountNumber)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(InvalidTransferException.class, () -> 
            accountService.transfer(accountNumber, accountNumber, transferAmount)
        );
    }

    @Test
    void testTransferNegativeAmountFails() {
        // Given
        String sourceAccountNumber = "123";
        String destinationAccountNumber = "456";
        BigDecimal transferAmount = new BigDecimal("-50.00"); // Negative amount
        BigDecimal sourceInitialBalance = new BigDecimal("200.00");
        BigDecimal destinationInitialBalance = new BigDecimal("150.00");

        Account sourceAccount = new Account(sourceAccountNumber, sourceInitialBalance, "John", "Doe");
        Account destinationAccount = new Account(destinationAccountNumber, destinationInitialBalance, "Jane", "Roe");

        when(accountRepo.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(destinationAccountNumber)).thenReturn(Optional.of(destinationAccount));

        // When & Then
        assertThrows(InvalidTransferException.class, () -> 
            accountService.transfer(sourceAccountNumber, destinationAccountNumber, transferAmount)
        );
    }

    @Test
    void testSuccessfulDeposit() throws Exception {
        // Given
        String accountNumber = "123";
        BigDecimal initialBalance = new BigDecimal("200.00");
        BigDecimal depositAmount = new BigDecimal("100.00");

        Account account = new Account(accountNumber, initialBalance, "John", "Doe");
        when(accountRepo.findById(accountNumber)).thenReturn(java.util.Optional.of(account));

        // When
        BigDecimal newBalance = accountService.deposit(accountNumber, depositAmount);

        // Then
        assertEquals(initialBalance.add(depositAmount), newBalance);
        assertEquals(initialBalance.add(depositAmount), account.getBalance());

        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testDepositWithInvalidAccountNumber() {
        // Given
        String accountNumber = "123";
        BigDecimal depositAmount = new BigDecimal("100.00");

        when(accountRepo.findById(accountNumber)).thenReturn(Optional.empty()); // Account not found

        // When & Then
        assertThrows(InvalidAccountException.class, () -> 
            accountService.deposit(accountNumber, depositAmount)
        );
    }

    @Test
    void testDepositWithNegativeAmount() {
        // Given
        String accountNumber = "123";
        BigDecimal depositAmount = new BigDecimal("-50.00"); // Negative amount

        // When & Then
        assertThrows(InvalidTransferException.class, () -> 
            accountService.deposit(accountNumber, depositAmount)
        );
    }

    @Test
    void testDepositWithZeroAmount() {
        // Given
        String accountNumber = "123";
        BigDecimal depositAmount = BigDecimal.ZERO; // Zero amount

        // When & Then
        assertThrows(InvalidTransferException.class, () -> 
            accountService.deposit(accountNumber, depositAmount)
        );
    }
}