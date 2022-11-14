package com.atguigu.yygh.common.utils;

public class JwtTest {
	public static void main(String[] args) {
		String token = JwtHelper.createToken(123L, "tom");
		System.out.println("token = " + token);

		//1、头 Header 2、3、
		String getToken = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDOzMDE3sTQz1lEqLU4t8kwBihlBOX6JualATSX5uUq1AHB2Uo1CAAAA.QQ0ZMHk2KKK4TKnoFwimn0IQOaEfiGkCXLdvnZa5YjWLd95FZYRNcK0Gz3DfP5DLpurO9s3EvB_sj9rfutUMtA";
		Long userId = JwtHelper.getUserId(getToken);
		String userName = JwtHelper.getUserName(getToken);
		System.out.println("userName = " + userName);
		System.out.println("userId = " + userId);
	}
}
