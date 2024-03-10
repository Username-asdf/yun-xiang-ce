package com.umi.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aliyuncs.ecs.model.v20140526.CopyImageResponse;
import com.aliyuncs.imagerecog.model.v20190930.TaggingImageResponse;
import com.umi.mapper.UploadMapper;
import com.umi.pojo.Folder;
import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;
import com.umi.pojo.UploadExample;
import com.umi.pojo.Users;
import com.umi.service.AiYunService;
import com.umi.service.FolderService;
import com.umi.service.UploadService;
import com.umi.utils.ImageUtils;
import com.umi.utils.JsonUtils;
import com.umi.utils.ZipUtils;

@Service
@Transactional
public class UploadServiceImpl implements UploadService {

	@Autowired
	private UploadMapper uploadMapper;
	@Autowired
	private FolderService folderServiceImpl;
	@Autowired
	private AiYunService aiYunServiceImpl;
	@Autowired
	private OssAndTaggingImg oat;
	@Value("${filePath}")
	private String path;
	@Value("${imgWidth}")
	private int imgWidth;
	@Value("${imgHeight}")
	private int imgHeight;

	@Override
	public RespResult saveImage(int autoClassify,int fid, MultipartFile file) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("上传失败");
		Object principal = SecurityUtils.getSubject().getPrincipal();
		// 判断是否登录
		if (principal == null) {
			return result;
		}
		Users user = (Users) principal;
		// 判断文件是否为空
		if (file.isEmpty()) {
			return result;
		}

		String imgName = file.getOriginalFilename();
		String newImageName = UUID.randomUUID().toString() + imgName.substring(imgName.lastIndexOf("."));
		try {
			// 保存图片
			File f = new File(this.path + "/" + user.getUsername(), newImageName);
			if (!f.exists()) {
				f.mkdirs();
			}

			file.transferTo(f);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		
		Upload upload = new Upload();
		upload.setImgname(imgName);
		upload.setIsdel((byte) 0);
		upload.setRealname(newImageName);
		upload.setSize(file.getSize());
		upload.setUid(user.getId());
		upload.setUploadtime(new Date());
		
		// 判断上传的是否是图片
		boolean isimage = ImageUtils.isImage(imgName);
		String uploadFileUrl = null;
		if (isimage) {
			upload.setIsimg((byte) 1);// 设置为图片
			
			//缩略图
			String uploadPath = this.path+"/"+user.getUsername()+"/";
			File f = new File(uploadPath + "suolue/");
			if(!f.exists()) {
				f.mkdirs();
			}
			//生成缩略图
			ImageUtils.reduceImg(uploadPath+newImageName, 
					f.getAbsolutePath()+"/"+newImageName, 
					this.imgWidth, this.imgHeight, 0f);
		
			//将缩略图片上传到阿里云服务器
			uploadFileUrl = oat.uploadfile(this.path+"/"+user.getUsername()+"/suolue/"+newImageName,
					user.getUsername()+"/"+newImageName);
			if(uploadFileUrl == null || uploadFileUrl.equals("")) {
				throw new RuntimeException("上传图片到阿里云服务器失败");
			}
			
			System.out.println(uploadFileUrl);
			//设置tags
			upload.setTags(JsonUtils.objectToJson(oat.getScene(uploadFileUrl)));
		}else {
			upload.setIsimg((byte) 0);
		}
		
		//设置fid
		if(autoClassify > 0) {
			int i = this.aiYunServiceImpl.oneFileAutoClassify(upload);
			if(i < 0) {
				throw new RuntimeException("上传文件时,自动归类失败");
			}
		}else {
			upload.setFid(fid);// 未创建文件夹 上传到根目录
		}
		
		int i = this.insUpload(upload);
		if (i > 0) {
			result.setCode(200);
			result.setMsg("上传成功");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("src", newImageName);
			result.setData(data);
		}
		
		return result;
	}

	@Override
	public int insUpload(Upload upload) {

		return this.uploadMapper.insertSelective(upload);
	}

	@Override
	public long selCountByFid(int uid, int fid) {
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid).andIsdelEqualTo((byte) 0);
		return this.uploadMapper.countByExample(example);
	}

	@Override
	public void getSmallImage(HttpServletResponse resp, String imgName) {
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		
		if (imgName == null || imgName.equals("")) {
			return;
		}
		File file = new File(this.path+"/"+user.getUsername()+"/suolue/"+imgName);
		if(file.exists()) {
			this.readFileToResp(resp, user.getUsername()+"/suolue", imgName);
		}else {
			this.getImage(resp, imgName);
		}
	}

	@Override
	public void getImage(HttpServletResponse resp, String imgName) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return;
		}
		Users user = (Users) principal;

		if (imgName == null || imgName.equals("")) {
			return;
		}

		this.readFileToResp(resp, user.getUsername(), imgName);

	}

	// 读取文件到响应流中
	private void readFileToResp(HttpServletResponse resp, String folderName, String fname) {
		File img = new File(this.path + "/" + folderName + "/" + fname);
		if (!img.exists()) {
			return;
		}
		
		resp.setContentLength((int) img.length());
		try (FileInputStream fis = new FileInputStream(img);) {
			byte[] b = new byte[fis.available()];
			while (fis.read(b) != -1) {
				resp.getOutputStream().write(b);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}finally {
			try {
				resp.getOutputStream().flush();
				resp.getOutputStream().close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int delUploadById(long id, int uid) {
		Upload upload = uploadMapper.selectByPrimaryKey(id);
		if (upload == null) {
			return 0;
		}

		if (upload.getUid() != uid) {
			return 0;
		}

		Upload record = new Upload();
		record.setId(id);
		record.setIsdel((byte) 1);
		return uploadMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public RespResult delUpload(long id) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("删除失败");

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return result;
		}
		Users user = (Users) principal;

		int i = delUploadById(id, user.getId());
		if (i > 0) {
			result.setCode(200);
			result.setMsg("删除成功");
		}

		return result;
	}

	@Override
	public int delUploadByFid(int fid, int uid) {
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid);

		Upload record = new Upload();
		record.setIsdel((byte) 1);
		return uploadMapper.updateByExampleSelective(record, example);
	}

	@Override
	public void downloadFile(HttpServletResponse resp, String filename) {
		if (filename == null || filename.equals("")) {
			return;
		}

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return;
		}
		Users user = (Users) principal;

		// 设置下载响应头
		resp.setHeader("Content-Disposition", "attachment;filename=" + filename);
		// 告知浏览器文件的大小
		resp.addHeader("Content-Length", "" + new File(this.path + "/" + user.getUsername() + "/" + filename).length());
//	        resp.setContentType("application/octet-stream");
		// 将文件读取到输出流
		this.readFileToResp(resp, user.getUsername(), filename);

	}

	@Override
	public void downloadZip(HttpServletResponse resp, int[] folderIdList, String[] fileList) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return;
		}
		Users user = (Users) principal;

		// 拷贝文件夹
		if (folderIdList != null && folderIdList.length > 0) {
			List<Folder> folderList = new ArrayList<Folder>();
			for (int i = 0; i < folderIdList.length; i++) {
				Folder folder = this.folderServiceImpl.selFolderById(folderIdList[i]);
				if (folder != null) {
					folderList.add(folder);
				}
			}
			this.folderServiceImpl.copyFolderToTemp(user, folderList, "");
		}
		// 拷贝文件
		if (fileList != null && fileList.length > 0) {
			for (String fileName : fileList) {
				try {
					ZipUtils.copyFile(this.path + "/" + user.getUsername() + "/" + fileName,
							this.path + "/" + user.getUsername() + "/temp/" + fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// 下载zip
		String downloadPath = this.path + "/" + user.getUsername() + "/temp";
		try {
			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", "attachment; filename=tupian.zip");
			ZipUtils.toZip(downloadPath, resp.getOutputStream(), true);
		} catch (RuntimeException | IOException e) {
			e.printStackTrace();
		}

		// 删除临时文件夹
		ZipUtils.deleteFile(new File(downloadPath));
	}

	@Override
	public List<Upload> selUploadByFid(int uid, int fid) {
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid);
		return uploadMapper.selectByExample(example);
	}

	@Override
	public void downloadZipFormDb(HttpServletResponse resp, int[] folderIdList, String[] fileList) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return;
		}
		Users user = (Users) principal;

		List<String> fl = new ArrayList<String>();
		if (fileList != null && fileList.length > 0) {
			fl = Arrays.asList(fileList);
		}
		List<Upload> folderList = new ArrayList<Upload>();
		if (folderIdList != null && folderIdList.length > 0) {
			for (int i = 0; i < folderIdList.length; i++) {
				Folder folder = this.folderServiceImpl.selFolderById(folderIdList[i]);
				if (folder != null) {
					Upload upload = new Upload();
					upload.setIsimg((byte)2);
					upload.setId((long)folder.getId());
					upload.setImgname(folder.getFname());
					upload.setRealname(folder.getFname());
					upload.setUid(user.getId());
					folderList.add(upload);
				}
			}
		}
		try {
			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", "attachment; filename=tupian.zip");
			// 下载zip
			this.folderServiceImpl.toZip(user, new ZipOutputStream(resp.getOutputStream()), folderList, fl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
	}

	@Override
	public Upload selUploadByIdUpload(long id) {
		return this.uploadMapper.selectByPrimaryKey(id);
	}

	@Override
	public int updUploadById(Upload upload) {
		return this.uploadMapper.updateByPrimaryKeySelective(upload);
	}

	@Override
	public RespResult updUploadImgName(String fname, long id) {
		RespResult result = new RespResult(500 ,"修改失败", null);
		
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		
		Upload upload = this.selUploadByIdUpload(id);
		if(upload == null || upload.getUid().intValue() != user.getId().intValue()) {
			return result;
		}
		//修改文件名
		upload.setImgname(fname);
		int i = this.updUploadById(upload);
		if(i > 0) {
			result.setCode(200);
			result.setMsg("修改成功");
		}
		return result;
	}

	@Override
	public RespResult delAlreadyDeleteFile() {
		RespResult result = new RespResult(200,"删除成功",null);
		
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(user.getId()).andIsdelEqualTo((byte)1);
		List<Upload> list = this.uploadMapper.selectByExample(example );
		String uploadPath = this.path + "/" +user.getUsername();
		for (Upload upload : list) {
			//删除硬盘上的文件
			new File(uploadPath + "/" +upload.getRealname()).delete();
			new File(uploadPath + "/suolue/" + upload.getRealname()).delete();
			//删除数据库的文件
			this.uploadMapper.deleteByPrimaryKey(upload.getId());
		}
		return result;
	}

	@Override
	public List<Upload> selUploadByUid(int uid) {
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(uid).andIsdelEqualTo((byte)0);
		return this.uploadMapper.selectByExampleWithBLOBs(example );
	}

	@Override
	public Upload selOneUploadByFid(int fid) {
		UploadExample example = new UploadExample();
		example.or().andFidEqualTo(fid);
		example.setOrderByClause("id desc");
		example.setOffset(0);
		example.setLimit(1);
		List<Upload> list = this.uploadMapper.selectByExample(example );
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<Upload> selUploadByFidWithBlob(int uid, int fid) {
		List<Upload> retList = new ArrayList<Upload>();
		
		//添加当前文件夹下的文件
		UploadExample example = new UploadExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid);
		retList.addAll(this.uploadMapper.selectByExampleWithBLOBs(example));
		//添加子文件夹下的文件
		List<Folder> folderList = this.folderServiceImpl.selAllFolderByFid(uid, fid);
		for (Folder folder : folderList) {
			example.clear();
			example.or().andUidEqualTo(uid).andFidEqualTo(folder.getId());
			retList.addAll(this.uploadMapper.selectByExampleWithBLOBs(example));
		}
		
		return retList;
	}

}
