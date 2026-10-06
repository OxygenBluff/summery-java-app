package com.example.demo.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.entities.*;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.CartItemRequestDTO;
import com.example.demo.dtos.CartResponseDTO;
import com.example.demo.mappers.CartMapper;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.CustomizationRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

import static java.util.spi.ToolProvider.findFirst;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    
    
    private final CouponService couponService;
    
    private final CartMapper cartMapper;
    private final CustomizationRepository customizationRepo;


    public CartResponseDTO addItemToCart(User customer, CartItemRequestDTO request) {
        //  Get / Create Cart
        Cart cart = cartRepo.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepo.save(newCart);
                });

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }//TODO custom Exception !!

        //product + ITS SELECTEDDDD variants
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(()-> new RuntimeException("Product not found"));

        List<ProductVariant> selectedVariants = new ArrayList<>();
        if(request.getVariantIds() !=null && !request.getVariantIds().isEmpty()){
            selectedVariants = variantRepo.findAllById(request.getVariantIds());
        }

        int maxAvailableStock = calculateStock(product,selectedVariants); // wooop

        //  is already in the cart ??
        CartItem existingItem = cart.getLignes().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .filter(item -> {
                    //BOTH NULL ! (or ofc same id)
                    //udpated now SET
                    List<Long> itemVariantsIds = item.getVariants().stream()
                            .map(ProductVariant::getId)
                            .sorted()
                            .toList();

                    List<Long> requestVariantIds = request.getVariantIds() != null ?
                            request.getVariantIds().stream().sorted().toList() : new ArrayList<>();

                    return itemVariantsIds.equals(requestVariantIds);
                })
                .findFirst()
                .orElse(null);
                //1-Stream cart items
                //2-> fitler by productId
                //3-> find first -> optional <CartItem> COULD BE EMPTYYY
                //4-> orElse (means null) -> UNWRAPS THE CART ITME OBJCET
        //TODO OMG it unwraps

        if (existingItem != null) {
        	//STOCK !!
        	
        	int totalRequested = existingItem.getQuantite() + request.getQuantity();

            //BOTH already amount in cart + the ONE JUST REQUESTED
            //INSTEA OF DUPLICATES!
            if (totalRequested >maxAvailableStock ) {// no more existingItem.getProduct().getStock()
                throw new RuntimeException("Insufficient stock!");
                //no more  + existingItem.getProduct().getStock()
            }
            
            // Update quantity if already exists
            existingItem.setQuantite(existingItem.getQuantite() + request.getQuantity());
        } else {
            //  new CartItem
            //STOCK CHECK PLEASEE..

            if (request.getQuantity() > maxAvailableStock ) {// was > product.getStock()
                throw new RuntimeException("Insufficient stock!");
            }
            
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantite(request.getQuantity());
            newItem.setVariants(selectedVariants);

            //customizations to the cartItem 
            if (request.getCustomizationIds() != null && !request.getCustomizationIds().isEmpty()) {
                List<Customization> customizations = customizationRepo.findAllById(request.getCustomizationIds());
                newItem.setCustomizations(customizations);
            }
            
            cart.getLignes().add(newItem);
        }

        Cart savedCart = cartRepo.save(cart);
        return cartMapper.toResponseDTO(savedCart); // dto
    }

    //HELP -> calcualteStock alone
    //1- no vairants -> BASE STOCK
    //2- variants exist ? -> MINIMUM across selected variants

    public int calculateStock(Product product, List<ProductVariant> variants){
        if(variants!=null && !variants.isEmpty()){
            //MIN
            return variants.stream()
                    .mapToInt(ProductVariant::getStockSupplementaire)
                    .min()
                    .orElse(product.getStock());
        }
        return product.getStock(); // empty
    }
    
    //getCart 
    public CartResponseDTO getCart(User customer) {
    	//if has cart -> find it 
    	//else new empty DTO
    	
    	return cartRepo.findByCustomer(customer)
                .map(cartMapper::toResponseDTO)
                .orElseGet(() -> {
                	CartResponseDTO emptyCart = new CartResponseDTO();
                    emptyCart.setItems(new ArrayList<>());
                    emptyCart.setTotalCartPrice(0.0);
                    emptyCart.setDiscountAmount(0.0);
                    emptyCart.setFinalPrice(0.0);
                    return emptyCart;
                });
    }
    
    //update quantity
    public CartResponseDTO updateItemQuantity(User customer, Long itemId, Integer newQuantity) {
    	//which cart..
    	Cart cart = cartRepo.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    	
    	//ooh the filter
    	CartItem item = cart.getLignes().stream()
               //why not cartItemRepo.findById(itemId) TODO ?
                //->what if someone passes an itemId belongong to another user's cart ??
                .filter(i -> i.getId().equals(itemId))// filter -> STREAM OF MATCHESS COULD BE MANY MATCHING THE CONDITION ???
                .findFirst()// firstFirst -> taked first match, stops evaluating -> wraps in an Optional<CartItem>
                //TODO write down this is good
                .orElseThrow(() -> new RuntimeException("Item not found in the cart"));
        //orElseThrow unboxes ? the optional huh into a CartItem
    	
    	//STOCK
        int remainingStock =calculateStock(item.getProduct(),item.getVariants());
    	if (newQuantity >remainingStock ) {// was item.getProduct().getStock())
            throw new RuntimeException("Only " + remainingStock + " units left in stock!");
        }
    	
    	if (newQuantity <= 0) {
            // 0 = remove i guess ?
            cart.getLignes().remove(item);
        } else {
            item.setQuantite(newQuantity);
        }
    	
    	Cart savedCart = cartRepo.save(cart);
        return cartMapper.toResponseDTO(savedCart);
    }
    
    //delete item from cart -> return the new cart !!
    public CartResponseDTO removeItemFromCart(User customer, Long itemId) {
    	Cart cart = cartRepo.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    	
    	//again find item..
    	CartItem itemToRemove = cart.getLignes().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in your cart"));
    	
    	cart.getLignes().remove(itemToRemove);
    	
    	Cart savedCart = cartRepo.save(cart);
        return cartMapper.toResponseDTO(savedCart);
        
        //ORPHAN REMOVAL in cart.java!! 
        //item is DELTEED automaitcally in DB !
        //lese it satys with cart_id NULL
    	
    }
    
    //coupon stuff..
    //TODO persist the coupon...
    public CartResponseDTO applyCoupon(User customer, String code) {
    	Cart cart = cartRepo.findByCustomer(customer)
                .orElseThrow(()->new ResourceNotFoundException("Couldn't find cart."));

    	Coupon coupon = couponService.isCouponValid(code);

        cart.setAppliedCoupon(coupon); // JUST ATTACHING ...
        //another function to calculate better.. then it returns the cart hmmmmmmmmmm
        cartRepo.save(cart);

        return getCart(customer);

    }
    
    //and to remove the coupon... 
    public CartResponseDTO removeCoupon(User customer) {
    	//the ACTUAL cart pleasee this is .. whatever
        Cart cart = cartRepo.findByCustomer(customer)
                        .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));

        cart.setAppliedCoupon(null);

        //save it NOT THE ONE IN MEMORY
        Cart savedCart = cartRepo.save(cart);

        return cartMapper.toResponseDTO(savedCart);
    }
    
    
 
    
}
