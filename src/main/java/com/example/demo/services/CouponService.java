package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entities.Coupon;
import com.example.demo.repositories.CouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {
	private final CouponRepository couponRepo;
	
	//admin does this right. 
	public List<Coupon> getAllCoupons(){
	 return couponRepo.findAll(); 
	}
    
    public Coupon createCoupon(Coupon coupon) {

		//TODO : causes a massive java lang error because of Unique code constraint
		if( couponRepo.existsByCode(coupon.getCode())){
			throw new RuntimeException("A coupon with this code already exists.");
		}
		return couponRepo.save(coupon);
    }

    public void deleteCoupon(Long id) {
    	couponRepo.deleteById(id);
    }
    
    //valider un coupon 
    public Coupon isCouponValid(String code) {
    	Coupon coupon = couponRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    	
    	if (!coupon.isActif()) throw new RuntimeException("Coupon is disabled");
    	//TODO CUSTOM EXCEPTIONS ?
    	
    	if (coupon.getDateExpiration() != null && coupon.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon is expired");
        }
    	
    	if (coupon.getUsagesMax() != null && coupon.getUsagesActuels() >= coupon.getUsagesMax()) {
            throw new RuntimeException("Coupon Usage limit reached");
        }
    	
    	return coupon;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
