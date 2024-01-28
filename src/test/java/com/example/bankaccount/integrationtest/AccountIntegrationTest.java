package com.example.bankaccount.integrationtest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankaccount.model.Account;
import com.example.bankaccount.repo.AccountRepo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepo accountRepo;

    @Test
    public void testCreateAccount() throws Exception {
        // Perform an HTTP POST request to create an account
        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountNumber\": \"123\", \"balance\": 100.00, \"firstName\": \"John\", \"lastName\": \"Doe\"}")
        )
        .andExpect(status().isCreated());

        // Verify that the account was created in the database
        Optional<Account> account = accountRepo.findById("123");
        assertTrue(account.isPresent());
        assertEquals(new BigDecimal("100.00"), account.get().getBalance());
    }

    @Test
    public void testGetBalance() throws Exception {
        // Create an account with an initial balance
        Account account = new Account("123", new BigDecimal("200.00"), "John", "Doe");
        accountRepo.save(account);

        mockMvc.perform(get("/accounts/{accountNumber}/balance", "123"))
            .andExpect(status().isOk())
            .andExpect(content().string("Balance: 200.00"));
    }

    @Test
    public void testTransfer() throws Exception {
        // Create source and destination accounts with initial balances
        Account sourceAccount = new Account("123", new BigDecimal("200.00"), "John", "Doe");
        Account destinationAccount = new Account("456", new BigDecimal("150.00"), "Jane", "Roe");
        accountRepo.saveAll(Arrays.asList(sourceAccount, destinationAccount));
    
        mockMvc.perform(put("/accounts/{sourceAccountNumber}/transfer", "123")
            .contentType(MediaType.APPLICATION_JSON)
            .param("destinationAccountNumber", "456")
            .param("amount", "50.00")
        )
        .andExpect(status().isOk());
    
        // Verify updated balances in the database
        Optional<Account> updatedSourceAccount = accountRepo.findById("123");
        Optional<Account> updatedDestinationAccount = accountRepo.findById("456");
        assertTrue(updatedSourceAccount.isPresent());
        assertTrue(updatedDestinationAccount.isPresent());
        assertEquals(new BigDecimal("150.00"), updatedSourceAccount.get().getBalance());
        assertEquals(new BigDecimal("200.00"), updatedDestinationAccount.get().getBalance());
    }

    @Test
    public void testDeposit() throws Exception {
        // Create an account with an initial balance
        Account account = new Account("123", new BigDecimal("200.00"), "John", "Doe");
        accountRepo.save(account);
    
        mockMvc.perform(put("/accounts/{accountNumber}/deposit", "123")
            .contentType(MediaType.APPLICATION_JSON)
            .param("amount", "50.00")
        )
        .andExpect(status().isOk());
    
        // Verify updated balance in the database
        Optional<Account> updatedAccount = accountRepo.findById("123");
        assertTrue(updatedAccount.isPresent());
        assertEquals(new BigDecimal("250.00"), updatedAccount.get().getBalance());
    }
}
