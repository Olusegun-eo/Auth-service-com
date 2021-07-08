package com.waya.wayaauthenticationservice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.entity.BusinessType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.ResponsePojo;
import com.waya.wayaauthenticationservice.repository.BusinessTypeRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BusinessTypeService {
	
	@Autowired
	private BusinessTypeRepository businessTypeRepo;
	
	
	public ResponsePojo createBusinessType(BusinessType businessType) {
		try {
			BusinessType mBusinesstype = new BusinessType();
			mBusinesstype.setBusinessType(businessType.getBusinessType());
			mBusinesstype.setId(0L);
			businessTypeRepo.save(mBusinesstype);
			return ResponsePojo.response(true, "Created Successfully");
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
	
	public List<BusinessType> findAll() {
		return businessTypeRepo.findAll();
	}
	
	public ResponsePojo edit(BusinessType businessType) {
		try {
			return businessTypeRepo.findById(businessType.getId()).map(bus -> {
				bus.setBusinessType(businessType.getBusinessType());
				return ResponsePojo.response(true, "Updated Successfully");
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	public ResponsePojo delete(Long id) {
		try {
			return businessTypeRepo.findById(id).map(businessType -> {
				businessTypeRepo.delete(businessType);
				return ResponsePojo.response(true, "Deleted Successfully");
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
