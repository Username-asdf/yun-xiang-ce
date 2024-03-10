package com.umi.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.umi.pojo.RespResult;
import com.umi.service.UploadService;

@Controller
public class ImageController {

	@Autowired
	private UploadService uploadServiceImpl;
	
	/**
	 * 上传文件
	 * @param fid
	 * @param file
	 * @return
	 */
	@PostMapping("/upload")
	@ResponseBody
	public RespResult upload(
			@RequestParam(name = "autoClassify", defaultValue = "0") int autoClassify,
			@RequestParam(name="fid", defaultValue = "0") int fid, MultipartFile file) {
		return uploadServiceImpl.saveImage(autoClassify, fid, file);
	}
	
	/**
	 * 跳转到upload_image.html
	 * @return
	 */
	@RequestMapping("/uploadImage")
	public String toUploadImage() {
		return "upload_image";
	}
	
	/**
	 * 跳转到show_image.html
	 * @return
	 */
	@RequestMapping("/showImage")
	public String toShowImage() {
		return "show_image";
	}
	
	/**
	 * 获取图片缩略图
	 * @param resp
	 * @param imgName
	 * @throws IOException 
	 */
	@RequestMapping(value="/gsi",
			produces = {MediaType.IMAGE_JPEG_VALUE,
					MediaType.IMAGE_GIF_VALUE, 
					MediaType.IMAGE_PNG_VALUE})
	public void getSmallImage( HttpServletResponse resp, String imgName) throws IOException {
//		resp.setContentType("image/jpeg");
//		resp.setHeader("accept-ranges", "bytes");
//		resp.setHeader("Keep-Alive", "timeout=60");
//		resp.setHeader("Vary", "Origin");
//		resp.setHeader("Vary", "Access-Control-Request-Method");
//		resp.setHeader("Vary", "Access-Control-Request-Headers");
//		resp.setHeader("Cache-Control", "no-store");
//		resp.setHeader("Last-Modified", "Mon, 28 Mar 2022 14:08:42 GMT");
		
//		resp.setHeader("access-control-allow-origin", "*");
//		resp.setHeader("ohc-file-size", "17999");
		uploadServiceImpl.getSmallImage(resp, imgName);
	}
	
	/**
	 * 获取原图片
	 * @param resp
	 * @param imgName
	 */
	@RequestMapping("/gi")
	public void getImage(HttpServletResponse resp, String imgName) {
		uploadServiceImpl.getImage(resp, imgName);
	}
	/**
	 * 通过文件id删除文件
	 * @param id
	 * @return
	 */
	@RequestMapping("/di")
	@ResponseBody
	public RespResult delfile(long id) {
		RespResult resp = uploadServiceImpl.delUpload(id);
		this.delAlreadyDeletaFile();
		return resp;
	}
	/**
	 * 通过文件名下载一个文件
	 * @param resp
	 * @param fileList
	 */
	@RequestMapping("/downloadFile")
	public void downloadFile(HttpServletResponse resp, String filename) {
		System.out.println("download: "+filename);
		uploadServiceImpl.downloadFile(resp, filename);
	}
	/**
	 * 批量下载文件
	 * @param resp
	 * @param folderIdList
	 * @param fileList
	 */
	@RequestMapping("/dmf")
	public void downloadMulFile(HttpServletResponse resp, 
			@RequestParam(name="fil", required=false) int[] folderIdList,
			@RequestParam(name="fl", required=false) String[] fileList) {
		this.uploadServiceImpl.downloadZipFormDb(resp, folderIdList, fileList);
	}
	/**
	 * 修改文件名
	 * @param fname
	 * @param id
	 * @return
	 */
	@RequestMapping("/uin")
	@ResponseBody
	public RespResult updateFileName(String fname, long id) {
		return this.uploadServiceImpl.updUploadImgName(fname, id);
	}
	/**
	 * 删除已经删除的文件
	 * @return
	 */
	@RequestMapping("/dadf")
	@ResponseBody
	public RespResult delAlreadyDeletaFile() {
		return this.uploadServiceImpl.delAlreadyDeleteFile();
	}
}
