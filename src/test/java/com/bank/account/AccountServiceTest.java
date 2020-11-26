package com.bank.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.bank.account.model.Operation;
import com.bank.account.model.User;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.UserRepository;
import com.bank.account.service.AccountService;
import com.bank.account.service.AccountService.OperationType;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountServiceTest {
	@Autowired
	private AccountService accountService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void depositeTest() {

		/*
		 * US 1: In order to save money As a bank client I want to make a deposit in my
		 * account
		 */

		// Given
		User user = new User("MyUserNameTest");
		userRepository.save(user);

		// When
		accountService.deposite(10d, "MyUserNameTest", LocalDate.now());

		// Then
		assertThat(accountRepository.findAll(), Matchers.hasSize(1));
		assertEquals(10d, accountRepository.findAll().get(0).getAmount());
		assertEquals(AccountService.OperationType.DEPOSITE.getOperation(),
				accountRepository.findAll().get(0).getOperationType());

		// When
		accountService.deposite(5d, "MyUserNameTest", LocalDate.now());

		// Then
		assertThat(accountRepository.findAll(), Matchers.hasSize(2));
		assertEquals(5d, accountRepository.findAll().get(1).getAmount());
		assertEquals(AccountService.OperationType.DEPOSITE.getOperation(),
				accountRepository.findAll().get(1).getOperationType());

	}

	@Test
	void retrieveTest() throws AccountService.RetveiveNotAllowed {
		/*
		 * US 2: In order to retrieve some or all of my savings As a bank client I want
		 * to make a withdrawal from my account
		 */

		// Given
		User user = new User("MyUserNameTest");
		userRepository.save(user);

		// When

		LocalDate dateOperation = LocalDate.now();
		Assertions.assertThrows(AccountService.RetveiveNotAllowed.class, () -> {
			accountService.retveive(7d, "MyUserNameTest", dateOperation);
		});

		// When
		accountService.deposite(10d, "MyUserNameTest", dateOperation);
		accountService.retveive(8d, "MyUserNameTest", dateOperation);

		// Then
		assertThat(accountRepository.findAll(), Matchers.hasSize(2));
		assertEquals(10d, accountRepository.findAll().get(0).getAmount());
		assertEquals(AccountService.OperationType.DEPOSITE.getOperation(),
				accountRepository.findAll().get(0).getOperationType());

		assertEquals(8d, accountRepository.findAll().get(1).getAmount());
		assertEquals(AccountService.OperationType.RETRIEVE.getOperation(),
				accountRepository.findAll().get(1).getOperationType());

		// When
		accountService.retveive(2d, "MyUserNameTest", dateOperation);

		// Then
		assertThat(accountRepository.findAll(), Matchers.hasSize(3));
		assertEquals(2d, accountRepository.findAll().get(2).getAmount());
		assertEquals(AccountService.OperationType.RETRIEVE.getOperation(),
				accountRepository.findAll().get(2).getOperationType());

		// When
		Assertions.assertThrows(AccountService.RetveiveNotAllowed.class, () -> {
			accountService.retveive(1d, "MyUserNameTest", dateOperation);
		});

	}

	@Test
	void checkMyOperationsTest() throws Exception {
		/*
		 * In order to check my operations As a bank client I want to see the history
		 * (operation, date, amount, balance) of my operations
		 */

		// Given
		User user = new User("MyUserNameTest");
		userRepository.save(user);
		LocalDate dateOperation = LocalDate.now();
		accountService.deposite(10d, "MyUserNameTest", dateOperation);
		accountService.retveive(8d, "MyUserNameTest", dateOperation);
		accountService.deposite(10d, "MyUserNameTest", dateOperation);
		accountService.retveive(3d, "MyUserNameTest", dateOperation);

		// When
		List<Operation> operationList = accountRepository.findAccountByUserUserName("MyUserNameTest");
		
		// Then
		int operationNumber = operationList.size();
		assertEquals(4, operationNumber);

		assertEquals(AccountService.OperationType.DEPOSITE.getOperation(),
				accountRepository.findAll().get(0).getOperationType());
		assertEquals(dateOperation, accountRepository.findAll().get(0).getDate());

		assertEquals(10d, accountRepository.findAll().get(0).getAmount());

		assertEquals(AccountService.OperationType.RETRIEVE.getOperation(),
				accountRepository.findAll().get(1).getOperationType());
		assertEquals(dateOperation, accountRepository.findAll().get(1).getDate());

		assertEquals(8d, accountRepository.findAll().get(1).getAmount());
		assertEquals(AccountService.OperationType.DEPOSITE.getOperation(),
				accountRepository.findAll().get(2).getOperationType());
		assertEquals(dateOperation, accountRepository.findAll().get(2).getDate());

		assertEquals(10d, accountRepository.findAll().get(2).getAmount());
		assertEquals(AccountService.OperationType.RETRIEVE.getOperation(),
				accountRepository.findAll().get(3).getOperationType());
		assertEquals(dateOperation, accountRepository.findAll().get(3).getDate());

		assertEquals(3d, accountRepository.findAll().get(3).getAmount());

		// Balance
		Double depositeSumm = operationList.stream()
				.filter(operation -> operation.getOperationType() == OperationType.DEPOSITE.getOperation())
				.collect(Collectors.summingDouble(Operation::getAmount));
		Double retrieveSumm = operationList.stream()
				.filter(operation -> operation.getOperationType() == OperationType.RETRIEVE.getOperation())
				.collect(Collectors.summingDouble(Operation::getAmount));
		double balance = depositeSumm - retrieveSumm;

		assertEquals(9, balance);

	}

	@AfterEach
	public void cleanUpEach() {
		accountRepository.deleteAll();
		userRepository.deleteAll();
	}

}
