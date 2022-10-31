package com.atguigu.yygh.hosp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/hosp/hospitalSet")
public class TestController {
	@GetMapping
	public List test1() {
		return new ArrayList<>();
	}
}
