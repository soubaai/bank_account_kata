package com.bank.account.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity

@Data
@NoArgsConstructor
public class Operation {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private LocalDate date;
	private Double amount;
	private String operationType;
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;

	public Operation(Double amount, User user) {
		super();
		this.amount = amount;
		this.user = user;
	}

	public Operation(LocalDate date, Double amount, String operationType, User user) {
		super();
		this.date = date;
		this.amount = amount;
		this.operationType = operationType;
		this.user = user;
	}

}
