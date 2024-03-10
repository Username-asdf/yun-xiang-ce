package com.umi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umi.pojo.Folder;
import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;
import com.umi.service.FolderService;

@RestController
public class FolderController {

	@Autowired
	private FolderService folderServiceImpl;
	
	/**
	 * 新增文件夹
	 * @param fid
	 * @param folderName
	 * @return
	 */
	@RequestMapping("/insf")
	public RespResult insNewFolder(Folder folder) {
		
		return folderServiceImpl.insFloder(folder);
	}
	
	/**
	 * 查询所有文件夹结构
	 * @return
	 */
	@RequestMapping("/selFolder")
	public RespResult selAllFolder() {
		return folderServiceImpl.selAllFolder();
	}
	
	/**
	 * 分页获取文件夹下的文件数据
	 * @param pageSize
	 * @param pageNum
	 * @param fid
	 * @return
	 */
	@RequestMapping("/gij")
	public List<Upload> showImgeByPage(@RequestParam(name="pageSize",defaultValue = "15")int pageSize,
			@RequestParam(name="pageNum",defaultValue = "1")int pageNum,
			@RequestParam(name="fid",defaultValue = "0")int fid){
		
		return folderServiceImpl.selFolderFileByLimit(pageNum, pageSize, fid);
	}
	
	/**
	 * 获取文件夹下文件数量
	 * @param fid
	 * @return
	 */
	@RequestMapping("/gic")
	public RespResult selCount(int fid) {
		return folderServiceImpl.selCurrentFolderFileCount(fid);
	}
	/**
	 * 删除文件夹
	 * @param fid
	 * @return
	 */
	@RequestMapping("/df")
	public RespResult delFolder(int fid) {
		return folderServiceImpl.delFolder(fid);
	}
	/**
	 * 修改文件夹名称
	 * @param fname
	 * @param id
	 * @return
	 */
	@RequestMapping("/ufn")
	public RespResult updateFolderName(String fname, int id) {
		return this.folderServiceImpl.updFolderName(id, fname);
	}
	/**
	 * 移动文件及文件夹
	 * @param fid
	 * @param folderList
	 * @param fileList
	 * @return
	 */
	@RequestMapping("/mv")
	public RespResult moveFolderAndFile(int fid,
			@RequestParam(name="folderList[]",required = false) int[] folderList,
			@RequestParam(name="fileList[]", required = false) long[] fileList) {
		return this.folderServiceImpl.moveFileAndFolder(fid, folderList, fileList);
	}
}
