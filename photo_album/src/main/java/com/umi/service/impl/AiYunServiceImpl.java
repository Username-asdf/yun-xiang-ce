package com.umi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliyuncs.imagerecog.model.v20190930.RecognizeSceneResponse;
import com.umi.pojo.Folder;
import com.umi.pojo.RespResult;
import com.umi.pojo.Upload;
import com.umi.pojo.Users;
import com.umi.service.AiYunService;
import com.umi.service.FolderService;
import com.umi.service.UploadService;
import com.umi.utils.JsonUtils;

@Service
@Transactional
public class AiYunServiceImpl implements AiYunService{

	@Autowired
	@Lazy
	private UploadService uploadServiceImpl;
	@Autowired
	@Lazy
	private FolderService folderServiceImpl;
	@Autowired
	private OssAndTaggingImg oat;
	@Autowired
	private FaceImpl faceImpl;
	@Value("${filePath}")
	private String path;
	
	@Override
	public RespResult autoClassify() {
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		
		//查询所有图片
		List<Upload> fileList = this.uploadServiceImpl.selUploadByUid(user.getId());
		
		int success = 0;
		for (Upload upload : fileList) {
			int i = this.oneFileAutoClassify(upload);
			if(i > 0) {
				//更新upload
				success += this.uploadServiceImpl.updUploadById(upload);
			}
		}
		
		RespResult result = new RespResult(500,"归类失败",null);
		
		if(success == fileList.size()) {
			result.setCode(200);
			result.setMsg("归类成功");
			this.folderServiceImpl.delAllEmptyFolder(user.getId(), 0); //删除空目录
		}
		
		return result;
	}

	@Override
	public int oneFileAutoClassify(Upload upload) {
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		int ret = 0;
		try {
			RecognizeSceneResponse resp = null;
			if(upload.getIsimg() == 1) { //图片处理
				if(upload.getTags()==null || upload.getTags().equals("") ||upload.getTags().equals("null")) {
					//查询图片tags
					resp = oat.getScene(oat.uploadfile(
							this.path+"/"+user.getUsername()+"/suolue/"+upload.getRealname(),
							user.getUsername()+"/"+upload.getRealname()));
					//添加到数据库
					if(resp.getData().getTags().size()>0) {
						upload.setTags(JsonUtils.objectToJson(resp));
					}else {
						return 0;
					}
				}else {
					resp = JsonUtils.jsonToPojo(upload.getTags(), RecognizeSceneResponse.class);
				}
				//进行文件夹操作
				Folder f = this.folderServiceImpl.selByFname(user.getId(), 0, oat.getSceneTag(resp));
				if(f == null) {
					f = new Folder();
					f.setUid(user.getId());
					f.setFid(0);
					f.setFname(oat.getSceneTag(resp));
					ret = this.folderServiceImpl.insFloderRetInt(f);
					if(ret <= 0) {
						return ret;
					}
				}
				upload.setFid(f.getId());
				
			}else { //视频处理
				//视频文件夹处理
				Folder folder = this.folderServiceImpl.selByFname(user.getId(), 0, "视频");
				if(folder == null) {
					//新增视频文件夹
					folder = new Folder();
					folder.setFid(0);
					folder.setUid(user.getId());
					folder.setFname("视频");
					ret = this.folderServiceImpl.insFloderRetInt(folder);
					if(ret <= 0) {
						return ret;
					}
				}
				//将视频归类到视频文件夹下
				upload.setFid(folder.getId());
			}
			
			return 1;
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Async
	@Override
	public void faceClassify() {
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		List<FaceImpl.FaceClassify> tempList = new ArrayList<FaceImpl.FaceClassify>();
		
		Folder folder = this.folderServiceImpl.selByFname(user.getId(), 0, "人物");
		List<Upload> uploadList = new ArrayList<Upload>();
		if(folder == null) { //没有查询到人物文件夹
			uploadList = this.uploadServiceImpl.selUploadByUid(user.getId());
		}else {
			uploadList = this.uploadServiceImpl.selUploadByFidWithBlob(user.getId(), folder.getId());
		}
		
		for (Upload upload : uploadList) { //生成需要传递的list对象
			if(upload.getIsimg() == 1) {
				if(upload.getTags()!=null && !upload.getTags().equals("")) {
					RecognizeSceneResponse rsr = JsonUtils.jsonToPojo(upload.getTags(), RecognizeSceneResponse.class);
					if(oat.getSceneTag(rsr).equals("人物")) {
						tempList.add(this.faceImpl.newFaceClassify(""+upload.getId(), 
								user.getUsername()+"/"+upload.getRealname()));
					}
				}
			}
		}
		
		List<List<String>> classifyList = this.faceImpl.faceClassify(tempList);

		//将人脸分类归类到  人物相册中
		if(folder == null) {  //没有人物folder 进行添加
			folder = new Folder();
			folder.setFid(0);
			folder.setFname("人物");
			folder.setUid(user.getId());
			int i = this.folderServiceImpl.insFloderRetInt(folder);
			if(i <= 0) {  //添加文件夹失败
				return ;
			}
		}
		
		
//		System.out.println("classifyList.size() = "+classifyList.size());
		
		for (List<String> l1 : classifyList) {
			
//			System.out.println("l1.size() = "+l1.size());
			
			Folder tempf = new Folder();
			tempf.setFid(folder.getId());
			tempf.setUid(user.getId());
			tempf.setFname(UUID.randomUUID().toString().replaceAll("-", ""));
			int i = this.folderServiceImpl.insFloderRetInt(tempf);
			if(i > 0) {
				for (String l2 : l1) {
					Upload u = new Upload();
					u.setId(Long.parseLong(l2));
					u.setFid(tempf.getId());
					this.uploadServiceImpl.updUploadById(u); //修改图片文件夹
				}
			}
		}
		
		this.folderServiceImpl.delAllEmptyFolder(user.getId(), folder.getId()); //删除空目录
	}
	
}
