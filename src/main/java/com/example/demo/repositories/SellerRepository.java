package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Seller;
import org.springframework.data.jpa.repository.Query;

public interface SellerRepository extends JpaRepository<Seller, Long> {
	Optional<Seller> findByUserId(Long userId);
	
	//more 
	List<Seller> findByActiveTrue(); // get all active branches!
    Optional<Seller> findByEmail(String email);

	boolean existsByNomBoutique(String nomBoutique);

	//to return the map of sellerId -> seller Names to
	//1: SHOW the names on front end
	//2-FILER BY ID only unfortuantely so..
	//TODO NO MAPS in JPA naurrr...
	@Query(
			"SELECT s.id, s.nomBoutique FROM Seller s "
	)
	List<Object[]> findIdAndNomBoutique();
}


