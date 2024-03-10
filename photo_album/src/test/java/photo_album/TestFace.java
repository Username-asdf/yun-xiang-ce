package photo_album;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.aliyuncs.facebody.model.v20191230.SearchFaceResponse;
import com.aliyuncs.facebody.model.v20191230.SearchFaceResponse.Data.MatchListItem;
import com.aliyuncs.facebody.model.v20191230.SearchFaceResponse.Data.MatchListItem.FaceItemsItem;
import com.aliyuncs.imagerecog.model.v20190930.RecognizeSceneResponse;
import com.umi.App;
import com.umi.pojo.Folder;
import com.umi.pojo.Upload;
import com.umi.pojo.Users;
import com.umi.service.FolderService;
import com.umi.service.UploadService;
import com.umi.service.impl.FaceImpl;
import com.umi.service.impl.OssAndTaggingImg;
import com.umi.utils.JsonUtils;

@SpringBootTest(classes = {App.class})
public class TestFace {

	@Autowired
	private OssAndTaggingImg oat;
	@Autowired
	private FaceImpl faceImpl;
	@Autowired
	private UploadService uploadServiceImpl;
	@Autowired
	private FolderService folderServiceImpl;
//	
//	@Test
//	void demo() throws InterruptedException {
//
//		System.out.println("----------------------------");
//		
//		Users user = new Users();
//		user.setId(3);
//		user.setUsername("abcd");
//		
//		//asdf用户
//		List<Upload> list = uploadServiceImpl.selUploadByUid(user.getId());
//		List<String> imgNames = new ArrayList<String>(); //输入的imgNames列表
//		for (Upload upload : list) {
//			if(upload.getIsimg() == 1) { //图片
//				RecognizeSceneResponse rsr = JsonUtils.jsonToPojo(upload.getTags(), RecognizeSceneResponse.class);
//				if(oat.getSceneTag(rsr).equals("人物")) {
//					imgNames.add(user.getUsername()+"/"+upload.getRealname());
//				}
//			}
//		}
//		
//		System.out.println("imgNames.size() = "+imgNames.size());
//		
//		
//
//		String dbname = UUID.randomUUID().toString().replaceAll("-", "");
//		faceImpl.faceDbCreate(dbname);
//		// 将imgNames添加到人脸数据库
//		
//		if(imgNames.size()<0) { 
//			return;
//		}
//		
//		
//		int end = (int) Math.ceil(imgNames.size()/5.0);
//		for(int i=0;i<end;i++) {
//			//添加样本  -- 每个样本添加5条数据
//			String entityId = UUID.randomUUID().toString().replaceAll("-", "_");
//			faceImpl.AddFaceEntity(dbname, entityId, "");
//			
//			System.out.println("-------");
//			for(int j=0;j<5;j++) { //添加数据
//				int temp = i*5+j;
//				if(temp>=imgNames.size()) {
//					break;
//				}
//				String img = imgNames.get(temp); //图片名称   用户名+图片名
//				String url = oat.getUrl(img, 3600*100);
//				System.out.println("=="+url);
//				faceImpl.addFaceImg(dbname, url,
//						entityId, img);
//			}
//		}
//		
//		List<String> searchDone = new ArrayList<String>(); //已经被搜索的图片
//		List<List<String>> classify = new ArrayList<List<String>>();
//		for (String imgName : imgNames) {
//			boolean flag = false;
//			for (String done : searchDone) {
//				if(imgName.equals(done)) {
//					System.out.println("==");
//					flag = true;
//					break;
//				}
//			}
//			
//			if(flag) {
//				continue;
//			}
//			SearchFaceResponse resp = faceImpl.faceSearch(dbname, 
//					oat.getUrl(imgName, 3600*1000));
//			
//			Thread.sleep(500);
//			
//			if(resp == null || resp.getData().getMatchList().size()<=0) {
//				continue;
//			}
//			//将查询完的图片添加到searchDone
//			List<FaceItemsItem> faceItems = resp.getData().getMatchList().get(0).getFaceItems();
//			List<String> tempList = new ArrayList<String>();
//			for (FaceItemsItem item : faceItems) {
//				if(item.getScore() >= 0.6) {
//					boolean tempFlag = false;
//					for (String str : searchDone) {
//						if(str.equals(item.getExtraData())) {
//							tempFlag = true;
//							break;
//						}
//					}
//					if(!tempFlag) {
//						searchDone.add(item.getExtraData());
//						tempList.add(item.getExtraData());
//					}
//				}
//			}
//			if(tempList.size()>1) { //人脸搜索 大于两张图片进行返回
//				classify.add(tempList);
//			}
//		}
//		
//		System.out.println("==================================");
//		for (List<String> l1 : classify) {
//			System.out.println("------------");
//			for (String l2 : l1) {
//				System.out.println("-----"+l2);
//			}
//		}
//		
//		
//		faceImpl.clearfaceDb(dbname);
//	}
//
//	@Test
//	void demo2() {
////		ossadmin oss = new ossadmin();
////		List<String> filesquery = oss.filesquery("张三/");
//		OssAndTaggingImg oat = new OssAndTaggingImg();
//		List<String> list = oat.filesquery("");
//		for (String string : list) {
//			System.out.println(oat.getUrl(string, 3600 * 1000));
//		}
//	}
//
//	@Test
//	void demo3() {
//		String id = UUID.randomUUID().toString().replaceAll("-", "");
//		System.out.println(id);
//	}
//	
//	
//	@Test
//	void demo4() {
//		System.out.println("----------------");
//		List<Folder> list = this.folderServiceImpl.selAllFolderByFid(3, 80);
//		for (Folder folder : list) {
//			System.out.println(folder.getFname());
//		}
//	}
//	
//	@Test
//	void demo5() {
//		List<Upload> list = this.uploadServiceImpl.selUploadByFidWithBlob(3, 80);
//		System.out.println("list.size() = "+list.size());
//		for (Upload upload : list) {
//			System.out.println(upload.getRealname());
//		}
//	}
	
	
//	@Test
//	void demo6() {
//		int ret = this.uploadServiceImpl.delUploadByFid(61, 1);
//		System.out.println(ret);
//	}
}
