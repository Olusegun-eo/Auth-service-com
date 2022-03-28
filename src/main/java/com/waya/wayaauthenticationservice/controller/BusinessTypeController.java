package com.waya.wayaauthenticationservice.controller;

import java.util.List;
import java.util.Map;

import com.waya.wayaauthenticationservice.pojo.others.BusinessTypePojo;
import com.waya.wayaauthenticationservice.pojo.others.BusinessTypeUpdatePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.waya.wayaauthenticationservice.entity.BusinessType;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import com.waya.wayaauthenticationservice.service.impl.BusinessTypeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/business/type")
@Tag(name = "BUSINESS TYPE", description = "Business Type Service API")
public class BusinessTypeController {

	@Autowired
	private BusinessTypeService businessTypeService;

	@ApiOperation(value = "Create Business Type", notes = "Create Business Type",tags = { "BUSINESS TYPE" })
	@PostMapping("/create")
	public ResponseEntity<ResponsePojo> create(@Valid @RequestBody BusinessTypePojo businessType) {
		return ResponseEntity.ok(businessTypeService.createBusinessType(businessType));
	}
	
	@ApiOperation(value = "Edit Business Type", notes = "Edit Business Type",tags = { "BUSINESS TYPE" })
	@PutMapping("/edit")
	public ResponseEntity<ResponsePojo> edit(@Valid @RequestBody BusinessTypeUpdatePojo businessType) {
		return ResponseEntity.ok(businessTypeService.edit(businessType));
	}
	
	@ApiOperation(value = "Find All Business Type", notes = "Find All Business types",tags = { "BUSINESS TYPE" })
	@GetMapping("/find/all")
	public ResponseEntity<Map<String, Object>> findAll(@RequestParam(defaultValue = "0") int page,
													 @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(businessTypeService.findAll(page,size));
	}
	
	@ApiOperation(value = "Delete Business Type", notes = "Delete Business Type",tags = { "BUSINESS TYPE" })
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ResponsePojo> delete(@PathVariable("id") Long id) {
		return ResponseEntity.ok(businessTypeService.delete(id));
	}
}
