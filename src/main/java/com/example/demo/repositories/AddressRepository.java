package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Address;
import com.example.demo.entities.User;

public interface AddressRepository extends JpaRepository<Address, Long> {
	List<Address> findByUser(User user);
	//get all user addresses

	//exists ? ignoreCase ofc
	boolean existsByUserAndRueIgnoreCaseAndVilleIgnoreCaseAndCodePostalIgnoreCaseAndPaysIgnoreCase(
			User user, String rue, String ville, String CodePostal, String Pays
	);

	boolean existsByUser(User user);

	Optional<Address> findFirstByUser(User user);

}
