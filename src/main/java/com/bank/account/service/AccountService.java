package com.bank.account.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.account.Message;
import com.bank.account.model.Operation;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.UserRepository;

@Service
public class AccountService {
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private UserRepository userRepository;

	@Transactional
	public void deposite(double amount, String userName, LocalDate date) {

		crateOpertion(amount, userName, OperationType.DEPOSITE.getOperation(), date);

	}

	@Transactional
	public void retveive(double amount, String userName, LocalDate date) throws RetveiveNotAllowed {

		List<Operation> operationList = accountRepository.findAccountByUserUserName(userName);

		Double depositeSumm = operationList.stream()
				.filter(operation -> operation.getOperationType() == OperationType.DEPOSITE.getOperation())
				.collect(Collectors.summingDouble(Operation::getAmount));
		Double retrieveSumm = operationList.stream()
				.filter(operation -> operation.getOperationType() == OperationType.RETRIEVE.getOperation())
				.collect(Collectors.summingDouble(Operation::getAmount));
		double balance = depositeSumm - retrieveSumm - amount;

		if (balance < 0)
			throw new RetveiveNotAllowed(Message.RETRIEVE_NOT_ALLOWED);

		crateOpertion(amount, userName, OperationType.RETRIEVE.getOperation(), date);

	}

	private void crateOpertion(double amount, String userName, String operation, LocalDate date) {
		Operation account = new Operation(date, amount, operation, userRepository.findByUserName(userName));
		accountRepository.save(account);
	}

	public enum OperationType {

		DEPOSITE("deposite"), RETRIEVE("retrieve");

		OperationType(String operationType) {
			this.operation = operationType;
		}

		private String operation;

		public String getOperation() {
			return operation;
		}

	}

	public class RetveiveNotAllowed extends Exception {

		public RetveiveNotAllowed(String errorMessage) {
			super(errorMessage);
		}
	}

}
