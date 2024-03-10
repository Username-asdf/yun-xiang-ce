package com.umi.service;

import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;

public interface AiYunService {

	/**
	 * 进行图片自动归类
	 * @return
	 */
	RespResult autoClassify();
	/**
	 * 一个文件单独归类
	 */
	int oneFileAutoClassify(Upload upload);
	/**
	 * 根据人脸自动归类
	 * 异步处理
	 */
	void faceClassify();
}
