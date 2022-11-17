package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScheduleTest {
	@Autowired
	private ScheduleService scheduleService;

	@Test
	public void test() {
		//scheduleService.getBookingSchedule(1, 7, "10000", "");
		scheduleService.getBookingSchedule(2, 7, "10000", "200040878");
	}
}
