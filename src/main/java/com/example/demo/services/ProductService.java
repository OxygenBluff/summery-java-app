package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.CategoryResponseDTO;
import com.example.demo.dtos.CustomizationResponseDTO;
import com.example.demo.dtos.ProductRequestDTO;
import com.example.demo.dtos.ProductResponseDTO;
import com.example.demo.dtos.VariantResponseDTO;
import com.example.demo.entities.Category;
import com.example.demo.entities.Customization;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductVariant;
import com.example.demo.entities.Review;
import com.example.demo.entities.Seller;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mappers.CustomizationMapper;
import com.example.demo.mappers.ProductMapper;
import com.example.demo.mappers.ProductVariantMapper;
import com.example.demo.mappers.ReviewMapper;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.repositories.SellerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
	private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final SellerRepository sellerRepo;
    private final ReviewRepository reviewRepo;
    
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final ReviewMapper reviewMapper;
    
    private final CustomizationMapper customizationMapper;

    @Transactional 
    public ProductResponseDTO createProduct(ProductRequestDTO dto,Long userId) {
    	//to create new product -> find who is the seller from the id
    	Seller seller = sellerRepo.findByUserId(userId)
    			.orElseThrow(() -> new RuntimeException("Seller profile not found !"));
    	//TODO -> cahnge to CUSTOM EXCEPTION!! 
    	
    	//GET THE CATEGORIES
    	List<Category> categories=categoryRepo.findAllById(dto.getCategoryIds());
    	
    	//-> new product! 
    	Product product = Product.builder()
    			.nom(dto.getNom())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .prixPromo(dto.getPrixPromo())
                .stock(dto.getStock())
                .actif(true)
                .seller(seller)
                .categories(categories)
                .images(dto.getImages() != null ? dto.getImages() : new ArrayList<>()) // if list ? -> use it 
                //OTHERWISE MAKE AN EMPTY one , no null pointer pls!!
                .build();
    	
    	//LINK THE VARIANTS !
    	if (dto.getVariants()!=null && !dto.getVariants().isEmpty()) {
    		List<ProductVariant> variants = dto.getVariants().stream() // our trusty stream for mapping!!!
    				.map(vDTO -> ProductVariant.builder()
    						.attribut(vDTO.getAttribut())
                            .valeur(vDTO.getValeur())
                            .stockSupplementaire(vDTO.getStockSupplementaire())
                            .prixDelta(vDTO.getPrixDelta())
							.mandatory(vDTO.isMandatory())
                            .product(product) // LINKS IT
                            
                            //->Product entity -> mappedBy =product !!!
                            //so productVairant HOLDS THE FOREIGN KEY!
                            
                            //if you do this: product.setVariants(variants) ONLY
                            //->IT WILL NOT FILL THE PRODUCT_ID column inproudct_variants !!! 
                            //->.product(product) tells EACH VARIANT you belong to this product !
                            .build())	
    				.collect(Collectors.toList());

    		product.setVariants(variants);
    	}
    	
    	//LINK: customizations 
    	if (dto.getCustomizations()!=null && !dto.getCustomizations().isEmpty()) {
    		List<Customization> customizations = dto.getCustomizations().stream()
    				.map(cDTO -> Customization.builder()
    						.name(cDTO.getName())
    						.extraPrice(cDTO.getExtraPrice())
    	                    .available(cDTO.isAvailable())
    	                    .product(product) 
    	                    .build())
    					.toList();
    		
    		product.setCustomizations(customizations);
    	}

		//EVERY PRODUCT NEEDS A LOWEST PRICE no matter what
		recalculateLowestPrice(product);
    	
    	Product savedProduct= productRepo.save(product);
    	
    	return mapToResponseDTO(savedProduct);
    	
    }
    
    private ProductResponseDTO mapToResponseDTO(Product p) {
    	return ProductResponseDTO.builder()
    			.id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .prix(p.getPrix())
                .prixPromo(p.getPrixPromo())
                .stock(p.getStock())
                .actif(p.isActif())
                .dateCreation(p.getDateCreation())
                .images(p.getImages())
                .sellerId(p.getSeller().getId())
                //categories ? -> category repsonse dto
                .categories(p.getCategories().stream()
                        .map(c -> CategoryResponseDTO.builder().id(c.getId()).nom(c.getNom()).build())
                        .collect(Collectors.toList()))// category has a lsit of products 
                //ALSO products have a list of category !! RECURSION INFINITE! 
                //->BUILD A CategoryResponseDTO isndie the STREAM OMG!! set ONLY Id+ Nom -> cut the recusrion
                //-> stops there doesn't look into the prodicts inside cateogry 
                // THIS   IS     INSANE  TODO READ ONTO THIS MORE
                
                //variant -> variant response  DTO
                .variants(p.getVariants().stream()
                        .map(v -> VariantResponseDTO.builder()
                                .attribut(v.getAttribut())
                                .valeur(v.getValeur())
                                .prixDelta(v.getPrixDelta())
                                .stockSupplementaire(v.getStockSupplementaire())
                                .build())
                        //SAME FOR VAIRANTS TO CUT RECUSRION! 
                        //-> Product field not included 
                        .collect(Collectors.toList()))
                
                
                .customizations(p.getCustomizations() != null ?
                	    p.getCustomizations().stream()
                	        .map(c -> CustomizationResponseDTO.builder()
                	                .id(c.getId())
                	                .name(c.getName())
                	                .extraPrice(c.getExtraPrice())
                	                .available(c.isAvailable())
                	                .build())
                	        .toList() : new ArrayList<>())
                .build();
    	
    }  
    
    //soft delete -> actif = false: 
    
    public void softDelete(Long id) {
    	Product product = productRepo.findById(id)
    			.orElseThrow(()->new ResourceNotFoundException("Product not found"));
    	//TODO CUSTOM EXCEPTION!! 
    	
    	product.setActif(false); 
        productRepo.save(product);
    }
    
    //-> search 
    public List<ProductResponseDTO> search(String query) {
        
        List<Product> products = productRepo.searchProducts(query);
        
        // 2. Map those Entities->  DTOs 
        return products.stream()
                       .map(this::mapToResponseDTO)
                       .collect(Collectors.toList());
    }
    
    //->update, gets id + dto btw 
    @Transactional
    //why transactional ?->if soemthing fails in the method 
    //->IT ROLLS BACK everything !! 
    //-> + don't have to call .save() !! does it automoatically!
    
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
    	Product existingProduct = productRepo.findById(id)
    			.orElseThrow(()-> new ResourceNotFoundException("Product with id " + id + " cannot be found"));
    	
    	//if exists -> just updaet the fields from dto 
    	//easy fields 
    	existingProduct.setNom(dto.getNom());
    	existingProduct.setDescription(dto.getDescription());
    	existingProduct.setPrix(dto.getPrix());
    	existingProduct.setStock(dto.getStock());
    	
    	//how to update categories.. JSON sends new categories -> clear the existing ones
    	List<Category> categories = categoryRepo.findAllById(dto.getCategoryIds());
    	existingProduct.setCategories(categories);
    	
    	//images -> list right ? 
    	existingProduct.setImages(dto.getImages());
    	
    	//VRAIANTS ! one product -> Many variants !! 
    	
    	existingProduct.getVariants().clear();   //- OPRHAN REMOVAL! becomes null in the link!!
    	
    	if (dto.getVariants()!=null) {
    		dto.getVariants().stream()
    			.map(variantMapper::toEntity)
    			.forEach(variant->{
    				variant.setProduct(existingProduct); //
    				existingProduct.getVariants().add(variant);
    			});
  
    	}
    	//customizations 
    	existingProduct.getCustomizations().clear();
    	if(dto.getCustomizations()!=null) {
    		dto.getCustomizations().stream()
    		.map(customizationMapper::toEntity)
    		.forEach(customization ->{
    			customization.setProduct(existingProduct);
    			existingProduct.getCustomizations().add(customization);
    		});
    	}

		//EVERY PRODUCT NEEDS A LOWEST PRICE no matter what
		recalculateLowestPrice(existingProduct);

    	Product updated = productRepo.save(existingProduct);
    	return productMapper.toResponseDTO(updated);
    	
    }
    
    //get product {id}: details + variantes+avis + note moyenne 
    public ProductResponseDTO getDetails(Long id) {
    	Product product = productRepo.findById(id)
    			.orElseThrow(()->new ResourceNotFoundException("Product not found with id " + id));
    	
    	//if PRDOCUT INACTIVE ? soft delted -> customers can't see !
    	//how to see if user is admin.. +> security 
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	boolean isAdmin = auth != null && auth.getAuthorities().stream()
    			.anyMatch(a->a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SELLER"));
    	
    	//interesting function.. 
    	if (!product.isActif() && !isAdmin) {
    		throw new ResourceNotFoundException("Product is no longer available.");
    	}
    	
    	//note moyenne 
    	//review has product id -> get them all 
    	List<Review> reviews=isAdmin
    			? reviewRepo.findByProductId(id) //if admin OR seller here -> everything
    			: reviewRepo.findByProductIdAndApprouveTrue(id);
    	

    	
    	Double noteMoyenne = reviews.stream()
    			.filter(Review::isApprouve) // SO CLEAN! call withing stream to fitler by its function DAMN
    			.mapToInt(Review::getNote)
    			.average()
    			.orElse(0.0); 
    	
    	// -> rounding ? 2 virgules ?
    	noteMoyenne = Math.round(noteMoyenne*10)/10.0;//what a way to round ..
    	
    	
    	ProductResponseDTO dto=productMapper.toResponseDTO(product);
    	dto.setNoteMoyenne(noteMoyenne);
    	
    	//Product dto still missinf REVIEWS linking! 
    	dto.setReviews(reviews.stream()
    			.map(reviewMapper::toResponseDTO)
    			.toList()
    			);
    	
    	return dto;
    }
    
    //Top10 best sellers 
    public List<ProductResponseDTO> getTopSelling(){
    	//pageable ? -> PageRequest.of(pageNumber,PageSize) -> 0 and 10items 
    	List<Product> topProducts = productRepo.findTop10BestSelling(PageRequest.of(0, 10));
    	
    	//stream each to ProductResponseDTO oo
    	return topProducts.stream()
    			.map(productMapper::toResponseDTO)
    			.toList();
    }
    
    //PAGEEABLE for returning all products.. + FILTRES
    public Page<ProductResponseDTO> getAllProducts(
    		String q,
    		String category, 
    	    Double minPrice, 
    	    Double maxPrice,
    	    Double minNote,
    	    Long sellerId, 
    	    Boolean promo, 
    	    Pageable pageable
    		){
    	//will add @Query in repo HELL NAH 
    	Page<Product> productPage = productRepo.findWithFilters(
    			q,category, minPrice, maxPrice,minNote, sellerId, promo, pageable
    			);
    	
    	//page of entities now !! -> page of DTOS remember.. 
    	return productPage.map(productMapper::toResponseDTO);
    }

	public List<ProductResponseDTO> getDiscountedProducts(){
		//just convert
		return productRepo.findAllByPrixPromoIsNotNullAndActifTrue().stream()
				.map(productMapper::toResponseDTO)
				.toList();
	}

	//hmm
	public void recalculateLowestPrice(Product product){
		//promo price -> else basePrice + cheapest config
		//-> stream variants -> groupBy attribute -> get values -> min -> sum
		double cheapestVariantDelta = product.getVariants().stream()
				.collect(Collectors.groupingBy(ProductVariant::getAttribut))
				.values().stream()
				.mapToDouble(group->group.stream()
						.mapToDouble(variant-> variant.getPrixDelta() !=null ? variant.getPrixDelta() :0.0)
						.min().orElse(0.0))
				.sum();

		double productBasePrice = product.getPrixPromo() != null ? product.getPrixPromo(): product.getPrix();

		product.setLowestPrice(productBasePrice+cheapestVariantDelta);
		//note
		//-> .values() gives A COLLECTION OF A LIST (of product variant)
		//-> need to STREAM it to get the list .
		//mapToDouble group -> = we got a LIST .. stream it lol
		//-> get a stream of the ONE GROUP's variants.

	}

    
   
}

