package com.atguigu.yygh.common.excp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YyghException extends RuntimeException {
	private Integer code;
	private String msg;
}
