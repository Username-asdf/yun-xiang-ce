package com.umi.service.impl;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.facebody.model.v20191230.RecognizeActionRequest;
import com.aliyuncs.imagerecog.model.v20190930.RecognizeSceneRequest;
import com.aliyuncs.imagerecog.model.v20190930.RecognizeSceneResponse;
import com.aliyuncs.imagerecog.model.v20190930.TaggingImageRequest;
import com.aliyuncs.imagerecog.model.v20190930.TaggingImageResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.umi.utils.JsonUtils;

/**
 * 
 * 类描述：oss存储空间操作类
 * 图片打标签
 */
@Service
public class OssAndTaggingImg {

	// yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
	private String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
	// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
	private String accessKeyId = "LTAI5tQUSgU3VBdoDV659tcd";
	private String accessKeySecret = "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj";
	// 填写Bucket名称。
	private String bucketName = "image20210519";
	// 创建OSSClient实例。
	private OSS ossClient;

	public OssAndTaggingImg() {
		ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
	}
	
	// 识别图片类型 返回该图片标签
	public TaggingImageResponse puttag(String url) {
		
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
		IAcsClient client = new DefaultAcsClient(profile);
		TaggingImageRequest request = new TaggingImageRequest();
		request.setRegionId("cn-shanghai");
//        request.setImageURL("http://viapi-test.oss-cn-shanghai.aliyuncs.com/viapi-3.0domepic/facebody/RecognizeFace/RecognizeFace2.png");
		request.setImageURL(url);
		try {
			TaggingImageResponse response = client.getAcsResponse(request);
			
			System.out.println(JsonUtils.objectToJson(response));
			return response;
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			System.out.println(e.getErrMsg());
			e.printStackTrace();
		}
		return null;
	}

	//场景识别
	public RecognizeSceneResponse getScene(String url) {
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
		IAcsClient client = new DefaultAcsClient(profile);
		
		RecognizeSceneRequest request = new RecognizeSceneRequest();
		request.setImageURL(url);
		try {
			RecognizeSceneResponse resp = client.getAcsResponse(request);
			System.out.println(JsonUtils.objectToJson(resp));
			return resp;
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getSceneTag(RecognizeSceneResponse resp) {
		return resp.getData().getTags().get(0).getValue();
	}
	/**
	 * 方法描述：获取权重最大的标签值
	 */
	public String getTag(TaggingImageResponse response) {

		return response.getData().getTags().get(0).getValue();
	}

	// 文件上传 -- 成功返回url - 失败返回null
	public String uploadfile(String imgPath, String filename) {

		File file = new File(imgPath);
		if(!file.exists() || file.isDirectory()) {
			return null;
		}
		
		// 创建PutObjectRequest对象。
		// 填写Bucket名称、Object完整路径和本地文件的完整路径。Object完整路径中不能包含Bucket名称。
		// 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, file);
		
		// 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
		// ObjectMetadata metadata = new ObjectMetadata();
		// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS,
		// StorageClass.Standard.toString());
		// metadata.setObjectAcl(CannedAccessControlList.Private);
		// putObjectRequest.setMetadata(metadata);

		// 上传文件。
		PutObjectResult putObject = ossClient.putObject(putObjectRequest);

		// 生成URL
		return this.getUrl(filename, 3600*1000);
	}

	//文件上传
	public String uploadfile(String filename, InputStream is) {
		PutObjectRequest request = new PutObjectRequest(bucketName, filename, is);
		ossClient.putObject(request);
		
		return this.getUrl(filename, 3600*1000);
	}
	
	/**
	 * 上传问价 获取图片标签
	 * @param filename
	 * @param is
	 * @return
	 */
	public String uploadFileToGetTag(String filename, InputStream is) {
		return this.getTag(puttag(uploadfile(filename, is)));
	}
	
	public String uploadFileToGetTag(String imgPath, String filename) {
		return this.getTag(puttag(uploadfile(imgPath, filename)));
	}
	// 文件下载
	public boolean downloadfile(String filename, String loc) {
		
		// 填写不包含Bucket名称在内的Object完整路径，例如testfolder/exampleobject.txt。
		String objectName = filename;

		// 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
		boolean found = ossClient.doesObjectExist(bucketName, filename);
		// boolean found = ossClient.doesObjectExist("examplebucket",
		// "exampleobject.txt", isINoss);

		if (found) {
			// 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
			// 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
			if (ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(loc)) != null) {
				return true;
			} else {
				return false;
			}

		} else {
			// 如果文件不存在返回
			return false;
		}

	}

	// 获取图片url  url expiration 过期时间(单位毫秒)
	public String getUrl(String filename, long expiration) {

		// 生成URL
		URL url1 = ossClient.generatePresignedUrl(bucketName, filename, new Date(new Date().getTime()+expiration));
		if (url1 != null) {
			return url1.toString();
		}
		return null;
	}

	// 删除单个文件
	public boolean deletefile(String filename) {
		// 填写不包含Bucket名称在内的Object完整路径，例如testfolder/exampleobject.txt。
		String objectName = filename;

		// 创建OSSClient实例。
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

		// 删除文件。如需删除文件夹，请将ObjectName设置为对应的文件夹名称。如果文件夹非空，则需要将文件夹下的所有object删除后才能删除该文件夹。
		ossClient.deleteObject(bucketName, objectName);
		boolean found = ossClient.doesObjectExist(bucketName, filename);
		if (found) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 方法描述：批量删除
	 * 
	 * @param keys 文件名list
	 * @return 一个DeleteObjectsResult
	 */
	public List<String> deletefiles(List<String> keys) {

		

		// 删除文件。key等同于ObjectName，表示删除OSS文件时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。

		DeleteObjectsResult deleteObjectsResult = ossClient
				.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(keys));
		List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
		return deletedObjects;
	}

	/**
	 * 
	 * 方法描述：获取文件列表
	 * 
	 * @param prefix 指定文件名前缀
	 * @return 文件名list
	 */
	public List<String> filesquery(String prefix) {

		// 创建OSSClient实例。
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

		// 列举文件。如果不设置KeyPrefix，则列举存储空间下的所有文件。如果设置KeyPrefix，则列举包含指定前缀的文件。
		ListObjectsV2Result result = ossClient.listObjectsV2(bucketName, prefix);
		List<OSSObjectSummary> ossObjectSummaries = result.getObjectSummaries();
		List<String> list = new ArrayList<String>();
		for (OSSObjectSummary s : ossObjectSummaries) {
			System.out.println("\t" + s.getKey());
			list.add(s.getKey());

		}
		
		return list;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(ossClient!=null) {
			// 关闭OSSClient。
			ossClient.shutdown();
		}
	}
}
