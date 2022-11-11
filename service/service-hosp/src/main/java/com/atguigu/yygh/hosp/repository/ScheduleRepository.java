package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {
	Schedule findByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

	List<Schedule> findByHoscodeAndDepcode(String hoscode, String depcode);

	List<Schedule> findByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date date);
}
