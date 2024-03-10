package com.umi.service;

import java.util.List;
import java.util.zip.ZipOutputStream;

import com.umi.pojo.Folder;
import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;
import com.umi.pojo.Users;

public interface FolderService {

	/**
	 * 新增文件夹
	 * @param folder
	 * @return
	 */
	RespResult insFloder(Folder folder);
	/**
	 * 新增文件夹
	 * @param folder
	 * @return 新增记录的id
	 */
	int insFloderRetInt(Folder folder);
	/**
	 * 查询所有文件夹结构
	 * @return
	 */
	RespResult selAllFolder();
	/**
	 * 通过文件夹名称 查询当前目录下的文件夹
	 * @param Fname
	 * @return
	 */
	Folder selByFname(int uid,int fid, String fname);
	/**
	 * 分页查询fid文件夹下的文件夹及文件
	 * @param pageNum
	 * @param pageSize
	 * @param fid
	 * @return
	 */
	List<Upload> selFolderFileByLimit(int pageNum, int pageSize, int fid);
	/**
	 * 查询当前文件夹下所有文件的个数
	 * @param fid
	 * @return
	 */
	RespResult selCurrentFolderFileCount(int fid);
	/**
	 * 删除文件夹
	 * @param fid 文件夹id
	 * @return
	 */
	RespResult delFolder(int fid);
	/**
	 * 通过id 删除文件夹
	 * @param fid 文件夹id
	 * @param uid
	 * @return
	 */
	int delFolderById(int fid, int uid);
	/**
	 * 删除文件夹下所有文件
	 * @param fid  父文件夹id
	 * @param uid
	 * @return
	 */
	int delFoldderByFid(int fid, int uid);
	/**
	 * 把文件夹及文件结构拷贝到 temp目录
	 * @param uid
	 * @param folderList
	 * @param path  初始path必须为空
	 */
	void copyFolderToTemp(Users user, List<Folder> folderList ,String path);
	/**
	 * 从数据库中获取数据
	 * 进行压缩
	 * @param user
	 * @param zos
	 * @param folderList
	 * @param fileList
	 */
	void toZip(Users user,ZipOutputStream zos, List<Upload> folderList, List<String> fileList);
	/**
	 * 查询fid下的所有目录
	 * @param fid
	 * @return
	 */
	List<Folder> selFolderByFid(int uid, int fid);
	
	/**
	 * 通过id查询folder
	 * @param id
	 * @return
	 */
	Folder selFolderById(int id);
	/**
	 * 通过id修改folder
	 * @param folder
	 * @return
	 */
	int updFolderById(Folder folder);
	/**
	 * 修改文件夹名称
	 * @param id
	 * @param fname
	 * @return
	 */
	RespResult updFolderName(int id, String fname);
	/**
	 * 移动文件及文件夹到特定的目录
	 * @param fid
	 * @param folderList
	 * @param fileList
	 * @return
	 */
	RespResult moveFileAndFolder(int fid, int[] folderList, long[] fileList);
	/**
	 * 递归查询fid文件夹下的所有文件夹
	 * @param uid
	 * @param fid
	 * @return
	 */
	List<Folder> selAllFolderByFid(int uid, int fid);
	/**
	 * 递归删除fid目录下所有空目录
	 * @param uid
	 * @param fid
	 * @return 删除空目录的条数
	 */
	int delAllEmptyFolder(int uid, int fid);
}
