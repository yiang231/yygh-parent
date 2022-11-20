package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-cmn")
public interface DictFeignClient {
	@GetMapping("/admin/cmn/dict/getName/{value}/{dictCode}")
	public String getNameByValueAndDictCode(@PathVariable String value, @PathVariable String dictCode);

	@GetMapping("/admin/cmn/dict/getName/{value}")
	public String getNameByValue(@PathVariable String value);
}
