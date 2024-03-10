package com.umi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.umi.pojo.Upload;

public interface ShowViewMapper {

	/**
	 * 分页查询用户当前目录下的
	 * 所有文件及文件夹
	 * @param uid
	 * @param fid
	 * @return
	 */
	@Select("select id,uid,fid,isImg isimg,imgName imgname,realName realname,uploadTime uploadtime from upload where uid=#{uid} and "
			+ "fid=#{fid} and isDel=0 "
			+ "UNION all "
			+ "select id,uid,fid,2,fname,fname,DATE('1990/1/1') from folder where uid=#{uid} "
			+ "and fid=#{fid}")
	List<Upload> selFolderFile(int uid, int fid);
}
