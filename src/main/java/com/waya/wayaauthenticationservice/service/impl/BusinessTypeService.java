package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.BusinessType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.BusinessTypePojo;
import com.waya.wayaauthenticationservice.pojo.others.BusinessTypeUpdatePojo;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import com.waya.wayaauthenticationservice.repository.BusinessTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BusinessTypeService {

    @Autowired
    private BusinessTypeRepository businessTypeRepo;

    public ResponsePojo createBusinessType(BusinessTypePojo businessTypePojo) {

        try {
            if(businessTypeRepo.existsByBusinessTypeIgnoreCase(businessTypePojo.getBusinessType()))
                return ResponsePojo.response(false, "Business Type Exists Already");

            BusinessType business = new BusinessType();
            business.setBusinessType(businessTypePojo.getBusinessType());
//            business.setId(0L);
            businessTypeRepo.save(business);
            return ResponsePojo.response(true, "Created Successfully");
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public Map<String, Object> findAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<BusinessType> businessTypePage = businessTypeRepo.findAll(paging);
        List<BusinessType> businessTypeList = businessTypePage.getContent();
        Map<String, Object> response = new HashMap<>();
        response.put("businessTypeList", businessTypeList);
        response.put("currentPage", businessTypePage.getNumber());
        response.put("totalItems", businessTypePage.getTotalElements());
        response.put("totalPages", businessTypePage.getTotalPages());
        return response;
    }

    public ResponsePojo edit(BusinessTypeUpdatePojo businessTypeUpdatePojo) {
        try {

            //BusinessType businessType = new BusinessType();
            return businessTypeRepo.findById(businessTypeUpdatePojo.getId()).map(bus -> {
                bus.setBusinessType(businessTypeUpdatePojo.getBusinessType());
                businessTypeRepo.save(bus);
                return ResponsePojo.response(true, "Updated Successfully");
            }).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
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
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
