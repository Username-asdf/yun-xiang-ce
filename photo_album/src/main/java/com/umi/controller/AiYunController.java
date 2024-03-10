package com.umi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umi.pojo.RespResult;
import com.umi.service.AiYunService;

@RestController
public class AiYunController {

	@Autowired
	private AiYunService aiYunServiceImpl;
	
	/**
	 * 图片自动归类
	 * @return
	 */
	@RequestMapping("/ac")
	public RespResult autoClassify() {
		
		return this.aiYunServiceImpl.autoClassify();
	}
	/**
	 * 人脸归类
	 * @return
	 */
	@RequestMapping("/fc")
	public String faceClassify() {
		this.aiYunServiceImpl.faceClassify();
		return "";
	}
}
