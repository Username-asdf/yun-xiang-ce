package com.umi.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;

public interface UploadService {

	/**
	 * 保存视频及图片
	 * @param file
	 * @return
	 */
	RespResult saveImage(int autoClassify, int fid, MultipartFile file);
	
	/**
	 * 新增上传文件
	 * @param upload
	 * @return
	 */
	int insUpload(Upload upload);
	
	/**
	 * 查询文件夹下图片或视频的个数
	 * @param fid
	 * @return
	 */
	long selCountByFid(int uid, int fid);
	
	/**
	 * 获取缩略图
	 * @param resp
	 * @param imgName
	 */
	void getSmallImage(HttpServletResponse resp, String imgName);
	/**
	 * 获取原图片
	 * @param resp
	 * @param imgName
	 */
	void getImage(HttpServletResponse resp, String imgName);
	/**
	 * 通过id删除文件
	 * @param id
	 * @return
	 */
	int delUploadById(long uid,int id);
	/**
	 * 删除文件
	 * @param id
	 * @return
	 */
	RespResult delUpload(long id);
	/**
	 * 删除文件夹下所有文件
	 * @param fid
	 * @param uid
	 * @return
	 */
	int delUploadByFid(int fid, int uid);
	/**
	 * 下载文件
	 * @param fileList
	 */
	void downloadFile(HttpServletResponse resp, String filename);
	/**
	 * 批量下载文件
	 * 过程：先把文件复制到文件夹下，再进行压缩
	 * @param resp
	 * @param fileList 同一目录下的文件列表
	 * @param folderList 同一目录下文件夹列表
	 */
	void downloadZip(HttpServletResponse resp, int[] folderIdList, String[] fileList);
	/**
	 * 批量下载文件
	 * 过程：从数据库中读取文件结构，直接压缩
	 * @param resp
	 * @param folderIdList
	 * @param fileList
	 */
	void downloadZipFormDb(HttpServletResponse resp, int[] folderIdList, String[] fileList);
	/**
	 * 查询文件夹下所有文件
	 * @param uid
	 * @param fid
	 * @return
	 */
	List<Upload> selUploadByFid(int uid, int fid);
	/**
	 * 递归查询文件夹下所有文件
	 * 带有tags
	 * @param uid
	 * @param fid
	 * @return
	 */
	List<Upload> selUploadByFidWithBlob(int uid, int fid);
	/**
	 * 通过id查询upload
	 * @param id
	 * @return
	 */
	Upload selUploadByIdUpload(long id);
	/**
	 * 通过id修改
	 * @param upload
	 * @return
	 */
	int updUploadById(Upload upload);
	/**
	 * 修改文件名称
	 * @param fname 文件名称
	 * @param id
	 * @return
	 */
	RespResult updUploadImgName(String fname, long id);
	/**
	 * 删除数据库及硬盘上所有已被删除的文件
	 * @return
	 */
	RespResult delAlreadyDeleteFile();
	
	/**
	 * 查询用户所有文件
	 * @param uid
	 * @return
	 */
	List<Upload> selUploadByUid(int uid);
	/**
	 * 查询文件夹下最后一个添加的图片信息
	 * @return
	 */
	Upload selOneUploadByFid(int fid);
	
}
