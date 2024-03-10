package com.umi.pojo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespResult implements Serializable{

	//响应结果
	private int code;
	private String msg;
	private Object data;
}
