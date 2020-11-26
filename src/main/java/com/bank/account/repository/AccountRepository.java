
package com.bank.account.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bank.account.model.Operation;

@Repository
public
interface AccountRepository extends JpaRepository<Operation, Long> {

	List<Operation> findAccountByUserUserName(String userName);

}
