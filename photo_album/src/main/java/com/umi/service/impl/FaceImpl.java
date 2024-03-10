package com.umi.service.impl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.facebody.model.v20191230.AddFaceEntityRequest;
import com.aliyuncs.facebody.model.v20191230.AddFaceEntityResponse;
import com.aliyuncs.facebody.model.v20191230.AddFaceRequest;
import com.aliyuncs.facebody.model.v20191230.AddFaceResponse;
import com.aliyuncs.facebody.model.v20191230.CompareFaceRequest;
import com.aliyuncs.facebody.model.v20191230.CompareFaceResponse;
import com.aliyuncs.facebody.model.v20191230.CreateFaceDbRequest;
import com.aliyuncs.facebody.model.v20191230.CreateFaceDbResponse;
import com.aliyuncs.facebody.model.v20191230.DeleteFaceEntityRequest;
import com.aliyuncs.facebody.model.v20191230.DeleteFaceEntityResponse;
import com.aliyuncs.facebody.model.v20191230.DetectFaceRequest;
import com.aliyuncs.facebody.model.v20191230.DetectFaceResponse;
import com.aliyuncs.facebody.model.v20191230.ListFaceEntitiesRequest;
import com.aliyuncs.facebody.model.v20191230.ListFaceEntitiesResponse;
import com.aliyuncs.facebody.model.v20191230.ListFaceEntitiesResponse.Data.Entity;
import com.aliyuncs.facebody.model.v20191230.SearchFaceResponse.Data.MatchListItem.FaceItemsItem;
import com.aliyuncs.facebody.model.v20191230.SearchFaceRequest;
import com.aliyuncs.facebody.model.v20191230.SearchFaceResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */

/** 
 * 类描述：人脸识别操作类
 * 这里面需要改成自己阿里云的key和密匙！！！！
 * 原来使用的已经失效！！！！
 */
@Service
public class FaceImpl{
	@Autowired
	private OssAndTaggingImg oat;
	
	/**
	 * 人脸数据库创建
	 */
	public String faceDbCreate(String dbname) {
		// TODO Auto-generated method stub
		 DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
	        IAcsClient client = new DefaultAcsClient(profile);

	        CreateFaceDbRequest request = new CreateFaceDbRequest();
	        request.setRegionId("cn-shanghai");
	        request.setName(dbname);

	        try {
	            CreateFaceDbResponse response = client.getAcsResponse(request);
	            System.out.println(new Gson().toJson(response));
	            return new Gson().toJson(response);
	        } catch (ServerException e) {
	            e.printStackTrace();
	        } catch (ClientException e) {
	            System.out.println("ErrCode:" + e.getErrCode());
	            System.out.println("ErrMsg:" + e.getErrMsg());
	            System.out.println("RequestId:" + e.getRequestId());
	        }
		return null;
	}
	
	/**
	 * 
	 * 方法描述：添加人脸样本
	 * @param dbname 数据库名称
	 * @param EntityId 设置样本ID
	 * @param Label 设置标签
	 * @return json参数列表
	 */
	public AddFaceEntityResponse AddFaceEntity(String dbname, String EntityId, String Label) {
		// TODO Auto-generated method stub
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);

        AddFaceEntityRequest request = new AddFaceEntityRequest();
        
        request.setRegionId("cn-shanghai");
        request.setDbName(dbname);
        request.setEntityId(EntityId);
        request.setLabels(Label);

        try {
            AddFaceEntityResponse response = client.getAcsResponse(request);
            return response;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
		return null;
	}
	
	/**
	 * 
	 * 方法描述：
	 * @param dbname 数据库名称
	 * @param url 图片地址
	 * @param EntityId 样本Id
	 * @return 人脸的json参数
	 */
	public AddFaceResponse addFaceImg(String dbname, String url, String EntityId,String extraData) {
		// TODO Auto-generated method stub
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);

        AddFaceRequest request = new AddFaceRequest();
        request.setRegionId("cn-shanghai");
        request.setDbName(dbname);
        request.setImageUrl(url);
        request.setEntityId(EntityId);
        request.setExtraData(extraData);

        try {
            return client.getAcsResponse(request);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
		return null;
	}
	
	/**
	 * 
	 * 方法描述：搜索人脸
	 * @param dbname
	 * @param url
	 */
	public SearchFaceResponse faceSearch(String dbname, String url) {
		// TODO Auto-generated method stub
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);

        SearchFaceRequest request = new SearchFaceRequest();
        
        request.setRegionId("cn-shanghai");
        request.setImageUrl(url);
        request.setDbName(dbname);
        request.setLimit(30);

        try {
            SearchFaceResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            return response;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
		return null;
	}
	
	//1对1对比 返回相似分数
	public Float faceonebyone(String url1,String url2) {
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);

        CompareFaceRequest request = new CompareFaceRequest();
        request.setRegionId("cn-shanghai");
        request.setImageURLA(url1);
        request.setImageURLB(url2);

        try {
            CompareFaceResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            System.out.println("分数="+response.getData().getConfidence());
            return response.getData().getConfidence();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }

		
		return null;
		
	}

	public int faceScan(String url) {
		// TODO Auto-generated method stub
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);

        DetectFaceRequest request = new DetectFaceRequest();
        request.setImageURL(url);

        try {
            DetectFaceResponse response = client.getAcsResponse(request);
            int i = response.getData().getFaceProbabilityList().size();
            System.out.println(new Gson().toJson(response));
            return i;
            
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }

		return 0;
	}
	/**
	 * 
	 * 方法描述：查询人脸数据库列表
	 * @param dbname 
	 * @param limit 最大数量
	 * @return
	 */
	public ListFaceEntitiesResponse querfacelist(String dbname, int limit) {
		DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
        IAcsClient client = new DefaultAcsClient(profile);
        ListFaceEntitiesRequest request = new ListFaceEntitiesRequest();
        request.setDbName(dbname);
        request.setLimit(limit);
        try {
            ListFaceEntitiesResponse response = client.getAcsResponse(request);
            return response;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }

        return null;
    }
	/**
	 * 
	 * 方法描述：删除人脸样本
	 * @param dbname
	 * @param EntityId
	 */
	public String deletefaceE(String dbname,String EntityId) {
		 DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "LTAI5tQUSgU3VBdoDV659tcd", "3D0pm49t2cC6Pt7PLkOmJc0XOQ5OUj");
	        IAcsClient client = new DefaultAcsClient(profile);

	        DeleteFaceEntityRequest request = new DeleteFaceEntityRequest();
	        request.setDbName(dbname);
	        request.setEntityId(EntityId);
	        try {
	            DeleteFaceEntityResponse response = client.getAcsResponse(request);
	            return new Gson().toJson(response);
	        } catch (ServerException e) {
	            e.printStackTrace();
	        } catch (ClientException e) {
	            System.out.println("ErrCode:" + e.getErrCode());
	            System.out.println("ErrMsg:" + e.getErrMsg());
	            System.out.println("RequestId:" + e.getRequestId());
	        }
			return null;
	}
	/**
	 * 
	 * 方法描述：清空指定人脸数据库
	 * @param dbname
	 * @param limit
	 */
	public void clearfaceDb(String dbname) {
		ListFaceEntitiesResponse querfacelist = querfacelist(dbname, 10);
		List<Entity> entities = querfacelist.getData().getEntities();
		for (Entity entity : entities) {
			if (deletefaceE(dbname, entity.getEntityId())!=null) {
				System.out.println("成功删除EntytiId="+entity.getEntityId()+"的人脸样本");
			}else {
				System.out.println("EntytiId="+entity.getEntityId()+"的人脸样本未删除");
			}
		}
		if (querfacelist(dbname, 10).getData().getTotalCount()>0) {
			clearfaceDb(dbname);
		}else {
			System.out.println("数据库清除完毕");
		}
	}

	/**
	 * 通过人脸进行分类
	 * @param imgNames 需要进行分类的图片
	 * @return 分组后的图片id列表
	 */
	public List<List<String>> faceClassify(List<FaceClassify> imgNames){
		List<List<String>> classify = new ArrayList<List<String>>();
		String dbname = UUID.randomUUID().toString().replaceAll("-", "");
		this.faceDbCreate(dbname);
		// 将imgNames添加到人脸数据库
		
		if(imgNames.size()<0) { 
			return classify;
		}
		
		
		int end = (int) Math.ceil(imgNames.size()/5.0);
		for(int i=0;i<end;i++) {
			//添加样本  -- 每个样本添加5条数据
			String entityId = UUID.randomUUID().toString().replaceAll("-", "_");
			this.AddFaceEntity(dbname, entityId, "");
			
			for(int j=0;j<5;j++) { //添加数据
				int temp = i*5+j;
				if(temp>=imgNames.size()) {
					break;
				}
				FaceClassify img = imgNames.get(temp); //图片名称   用户名+图片名
				String url = oat.getUrl(img.getImgName(), 3600*100);
				this.addFaceImg(dbname, url,
						entityId, img.getId());
			}
		}
		
		List<String> searchDone = new ArrayList<String>(); //已经被搜索的图片
		for (FaceClassify imgName : imgNames) {
			boolean flag = false;
			for (String done : searchDone) {
				if(imgName.getId().equals(done)) {
					flag = true;
					break;
				}
			}
			
			if(flag) {
				continue;
			}
			SearchFaceResponse resp = this.faceSearch(dbname, 
					oat.getUrl(imgName.getImgName(), 3600*1000));
			
			if(resp == null || resp.getData().getMatchList().size()<=0) {
				continue;
			}
			//将查询完的图片添加到searchDone
			List<FaceItemsItem> faceItems = resp.getData().getMatchList().get(0).getFaceItems();
			List<String> tempList = new ArrayList<String>();
			for (FaceItemsItem item : faceItems) {
				if(item.getScore() >= 0.5) {
					boolean tempFlag = false;
					for (String str : searchDone) {
						if(str.equals(item.getExtraData())) {
							tempFlag = true;
							break;
						}
					}
					if(!tempFlag) {
						searchDone.add(item.getExtraData());
						tempList.add(item.getExtraData());
					}
				}
			}
			if(tempList.size()>1) { //人脸搜索 大于两张图片进行返回
				classify.add(tempList);
			}
		}
		
		this.clearfaceDb(dbname);
		return classify;
	}
	
	public FaceClassify newFaceClassify(String id, String imgName) {
		return new FaceClassify(id, imgName);
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class FaceClassify{ //用于传递图片名称与id
		private String id;
		private String imgName;
	}

}
