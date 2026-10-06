package com.example.demo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	Optional<Coupon> findByCode(String code);

	//exists
	Boolean existsByCode(String code);
}

