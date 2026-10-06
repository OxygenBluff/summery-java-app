package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Product;
import com.example.demo.entities.Seller;

public interface ProductRepository extends JpaRepository<Product,Long>,JpaSpecificationExecutor<Product> {
	//filtering -> specifications 
	
	//recherche full-texte : name +decription 
	
	@Query("SELECT p FROM Product p WHERE p.actif = true AND "+
			"(LOWER(p.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(p.description) LIKE LOWER (CONCAT('%', :query, '%')))")
	
	//% = anything , so anything query is in between
	List<Product> searchProducts(@Param("query")String query);
	//now the method searchProduts just executes that query oh !
	
	//TOP 10 BEST SELLING 
	//-> product DOES NOT hold units sold 
	//but Order Item does -> filter orders by this prpoduct -> SUM
	@Query("SELECT o.product FROM OrderItem o WHERE o.product.actif =true AND o.order.statut=StatutCommande.PAID GROUP BY o.product.id ORDER BY SUM(o.quantite) DESC ")
	List<Product> findTop10BestSelling(Pageable pageable);
	//JPQL DOES NOT HAVE LIMIT ?? -> paeagble then..
	
	 //alright ..
	//"use prix promo if the product is on promo OTHERWISE  fall back to regular prix)
	//how to transalte that ?
	//-> TODO: minPrice IS NULL OR CASE WHEN p.promo = true THEN p.prixPromo ELSE p.prix END => :minPrice
	//All products, + filtres .. category, price min/max, promo, sellerId
	//Postgres are you kidding me ? -> DEFAULT type in psotfres (fails to auto resolve type) = BYTE ? cast as string.. like this, CAST( AS TEXT)
	@Query("SELECT p FROM Product p WHERE "+
		    "(CAST(:q AS String) IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%',CAST(:q AS String), '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',CAST(:q AS String), '%'))) AND "+

			"(:category IS NULL OR EXISTS (SELECT c FROM p.categories c WHERE c.nom= :category)) AND "+

			"(:minPrice IS NULL OR p.lowestPrice >= :minPrice) AND " +

			"(:maxPrice IS NULL OR " +
				"p.lowestPrice <=:maxPrice) AND " +

			"(:sellerId IS NULL OR p.seller.id =:sellerId) AND "+

			"(:promo IS NULL OR :promo = false OR p.prixPromo IS NOT NULL) AND "+
			"(p.actif = true) AND " +
			"(:minNote IS NULL OR (SELECT AVG(r.note) FROM Review r WHERE r.product = p AND r.approuve = true) >= :minNote)")
			
	//oh my.. 
	Page<Product> findWithFilters(
			@Param("q") String q, 
		    @Param("category") String category, 
		    @Param("minPrice") Double minPrice, 
		    @Param("maxPrice") Double maxPrice, 
		    @Param("minNote") Double minNote,
		    @Param("sellerId") Long sellerId, 
		    @Param("promo") Boolean promo, 
		    Pageable pageable
			);
	//java type erausure again ??
	//otherwise it won't know where to put each param ...OMG..
	
	//CONDITIONAL JPQL is WHAT ?
	//-> :variable IS NULL
	//if we don't send a param -> it becomes NULL 
	// (:category IS NULL OR p.category.nom = :category)
	
	//OR = true is AT LEAST one is true !!!!!!
	
	//=> IF category given -> part 1 = FALSE (:category is NULL) 
	//-> DB checks the OTHER PART! -> filters 
	//IF catgeory tho not provided -> first part = true -> entire OR = true 
	//-> IGNORES THE ENTIRE FITLER !! 
	
	// WRITE  THIS   DOWN  O:
	
	
	
	List<Product> findBySellerAndStockLessThanEqual(Seller seller, int threshold);

	//featured = discounted products i guess
	List<Product> findAllByPrixPromoIsNotNullAndActifTrue();
	
	
	
	
	
			
			
}
