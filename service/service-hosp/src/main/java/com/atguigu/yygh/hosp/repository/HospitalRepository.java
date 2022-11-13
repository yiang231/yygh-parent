package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository//操作mongodb
public interface HospitalRepository extends MongoRepository<Hospital, String> {
	Hospital findByHoscode(String hoscode);

	List<Hospital> findByHosnameLike(String hosname);
}
