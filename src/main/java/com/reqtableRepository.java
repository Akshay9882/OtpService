package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface reqtableRepository extends JpaRepository<reqtable, Integer> {
	List<reqtable> findByContact(String num);	
}
