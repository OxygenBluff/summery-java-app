package com.example.demo.repositories;

import com.example.demo.entities.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	//JPA can generate JOINs on itself omg 
	//-> we wanna find if USER has ORDERITEM where prpoduct id is given (jsut existence)
	//->join user and orderitem 
	boolean existsByOrder_Customer_IdAndProduct_IdAndOrder_Statut(
			Long userId, 
	        Long productId, 
	        StatutCommande statut
			);
	
	
	// it customer not user ..
	// _ = JUMPS BETWEEN TABLES!! 
	//HOW ? ORDER ITEM does not have user id WHYYYY
	
	//Order -> goes from OrderItem to ORDER field
	//_User -> looks at the USER FIELD 
	//_id -> gets the id, STOPS: comapre this to.. userId paramter!
	// AND -> Product -> look at product field, (orderItem to Product table)
	//_id -> check the id , compares with SECOND paramter 
	//AND (condition 3) ->go back to order table 
	//->_status -> comapre parameter! 
	
	// 0.0 TODO 
	
}
