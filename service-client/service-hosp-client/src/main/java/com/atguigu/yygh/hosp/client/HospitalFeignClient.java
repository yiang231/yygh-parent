package com.atguigu.yygh.hosp.client;

import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp")
public interface HospitalFeignClient {
	@GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
	public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

	@GetMapping("/api/hosp/hospital/getApiUrlByHoscode/{hoscode}")
	public String getApiUrlByHoscode(@PathVariable String hoscode);
}
