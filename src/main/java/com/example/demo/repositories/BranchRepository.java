package com.example.demo.repositories;

import com.example.demo.entities.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Seller,Long> {
    List<Seller> findByActiveTrue();
}
