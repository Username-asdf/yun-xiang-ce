package com.umi.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.umi.mapper.FolderMapper;
import com.umi.mapper.ShowViewMapper;
import com.umi.pojo.Folder;
import com.umi.pojo.FolderExample;
import com.umi.pojo.RespResult;
import com.umi.pojo.SelectFolderTree;
import com.umi.pojo.Upload;
import com.umi.pojo.Users;
import com.umi.service.FolderService;
import com.umi.service.UploadService;
import com.umi.utils.ZipUtils;

@Service
@Transactional
public class FolderServiceImpl implements FolderService {

	@Autowired
	private FolderMapper folderMapper;
	@Autowired
	private ShowViewMapper showViewMapper;
	@Autowired
	private UploadService uploadServiceImpl;
	@Value("${filePath}")
	private String path;
	private static final int BUFFER_SIZE = 2 * 1024;

	@Override
	public RespResult insFloder(Folder folder) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("创建文件夹失败");

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return result;
		}

		Users user = (Users) principal;
		folder.setUid(user.getId());
		// TODO 检测父文件夹是否存
		// 检测当前文件夹是否重复
		Folder fTemp = this.selByFname(user.getId(), folder.getFid(), folder.getFname());
		if (fTemp != null) {
			result.setCode(501);
			result.setMsg("文件夹已存在");
			return result;
		}

		int i = folderMapper.insertSelective(folder);
		if (i > 0) {
			result.setCode(200);
			result.setMsg("创建文件夹成功");
			return result;
		}
		return result;
	}

	@Override
	public RespResult selAllFolder() {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("获取数据失败");

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return result;
		}
		Users user = (Users) principal;

		FolderExample example = new FolderExample();
		example.or().andUidEqualTo(user.getId()).andFidEqualTo(0);
		List<Folder> list = folderMapper.selectByExample(example);
		List<SelectFolderTree> sfList = selTree(user.getId(), list);

		// 根目录
		List<SelectFolderTree> genList = new ArrayList<SelectFolderTree>();
		SelectFolderTree gen = new SelectFolderTree();
		gen.setTitle("根目录");
		gen.setId(0);
		gen.setChildren(sfList);
		genList.add(gen);

		result.setCode(200);
		result.setMsg("获取数据成功");
		result.setData(genList);

		return result;
	}

	@Override
	public Folder selByFname(int uid, int fid, String fname) {
		FolderExample example = new FolderExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid).andFnameEqualTo(fname);
		List<Folder> list = folderMapper.selectByExample(example);
		if (list == null || list.size() <= 0) {
			return null;
		}
		return list.get(0);
	}

	// 递归查询文件夹结构
	private List<SelectFolderTree> selTree(int uid, List<Folder> fl) {
		List<SelectFolderTree> sfList = new ArrayList<SelectFolderTree>();

		for (Folder folder : fl) {
			SelectFolderTree sf = new SelectFolderTree();
			sf.setId(folder.getId());
			sf.setTitle(folder.getFname());

			sf.setChildren(selTree(uid, this.selFolderByFid(uid, folder.getId())));
			sfList.add(sf);
		}
		return sfList;
	}

	@Override
	public List<Folder> selFolderByFid(int uid, int fid) {
		FolderExample example = new FolderExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid);
		return folderMapper.selectByExample(example);
	}

	@Override
	public List<Upload> selFolderFileByLimit(int pageNum, int pageSize, int fid) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return new ArrayList<Upload>();
		}
		Users user = (Users) principal;

		PageHelper.startPage(pageNum, pageSize);
		PageHelper.orderBy("isImg desc,uploadTime desc,id desc");
		PageInfo<Upload> info = new PageInfo<Upload>(showViewMapper.selFolderFile(user.getId(), fid));

		List<Upload> list = info.getList();
		for (Upload upload : list) { //设置文件夹图片为最后一张图片
			if(upload.getIsimg().intValue() == 2) {
				Upload u = this.uploadServiceImpl.selOneUploadByFid(upload.getId().intValue());
				if(u != null) {
					upload.setRealname(u.getRealname());
				}
			}
		}
		
		return list;
	}

	@Override
	public RespResult selCurrentFolderFileCount(int fid) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("获取数据失败");

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return result;
		}
		Users user = (Users) principal;

		FolderExample example = new FolderExample();
		example.or().andUidEqualTo(user.getId()).andFidEqualTo(fid);
		long count1 = folderMapper.countByExample(example);
		long count2 = uploadServiceImpl.selCountByFid(user.getId(), fid);
		long total = count1 + count2;

		result.setCode(200);
		result.setMsg("获取数据成功");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("total", total);
		data.put("pageSize", 15);
		result.setData(data);

		return result;
	}

	@Override
	public RespResult delFolder(int fid) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("删除失败");

		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			return result;
		}
		Users user = (Users) principal;

		int i = this.delFolderById(fid, user.getId());
		if (i > 0) {
			result.setCode(200);
			result.setMsg("删除成功");

			// 删除该文件夹下所有文件 及文件夹
			this.delFoldderByFid(fid, user.getId());
		}
		return result;
	}

	@Override
	public int delFolderById(int fid, int uid) {
		FolderExample example = new FolderExample();
		example.or().andIdEqualTo(fid).andUidEqualTo(uid);
		return folderMapper.deleteByExample(example);
	}

	@Override
	public int delFoldderByFid(int fid, int uid) {
		// 删除当前文件夹下的文件
		uploadServiceImpl.delUploadByFid(fid, uid);
		
		FolderExample example = new FolderExample();
		example.or().andUidEqualTo(uid).andFidEqualTo(fid);
		List<Folder> list = folderMapper.selectByExample(example);
		for (Folder folder : list) {
			
			// 删除子文件夹
			this.delFoldderByFid(folder.getId(), uid);

		}
		// 删除当前文件夹
		this.delFolderById(fid, uid);

		return 0;
	}

	@Override
	public void copyFolderToTemp(Users user, List<Folder> folderList, String path) {
		String basicPath = this.path + "/" + user.getUsername();

		for (Folder folder : folderList) {
			String fpath = basicPath + "/" + "temp/" + path + folder.getFname();
			if (path == null || path.equals("")) {
				fpath = basicPath + "/" + "temp/" + folder.getFname();
			}
			// 创建文件夹及拷贝文件
			File f = new File(fpath);
			if (!f.exists()) {
				f.mkdirs();
			}
			List<Upload> uploadList = uploadServiceImpl.selUploadByFid(user.getId(), folder.getId());
			for (Upload u : uploadList) {
				try {
					ZipUtils.copyFile(basicPath + "/" + u.getRealname(), fpath + "/" + u.getRealname());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// 递归
			this.copyFolderToTemp(user, this.selFolderByFid(user.getId(), folder.getId()),
					path + folder.getFname() + "/");
		}
	}

	@Override
	public Folder selFolderById(int id) {
		return folderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 递归压缩方法
	 *
	 * @param sourceFile       源文件
	 * @param zos              zip输出流
	 * @param name             压缩后的名称
	 * @param KeepDirStructure 是否保留原来的目录结构, true:保留目录结构;
	 *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
	 * @throws Exception
	 *
	 */

	public void compress(Upload folder, ZipOutputStream zos, Users user, String name,
			boolean KeepDirStructure) throws Exception {

		byte[] buf = new byte[BUFFER_SIZE];
		if (folder.getIsimg() != 2) {// 文件
			// 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
			zos.putNextEntry(new ZipEntry(name));
			// copy文件到zip输出流中
			int len;
			FileInputStream in = new FileInputStream(this.path + "/" + user.getUsername() + "/" + folder.getRealname());
			while ((len = in.read(buf)) != -1) {
				zos.write(buf, 0, len);
			}
			// Complete the entry
			zos.closeEntry();
			in.close();
		} else {
			List<Upload> sfl = showViewMapper.selFolderFile(user.getId(), folder.getId().intValue());
			if (sfl == null || sfl.size() <= 0) {
				// 需要保留原来的文件结构时,需要对空文件夹进行处理
				if (KeepDirStructure) {
					// 空文件夹的处理
					zos.putNextEntry(new ZipEntry(name + "/"));
					// 没有文件，不需要文件的copy
					zos.closeEntry();
				}
			} else {
				for (Upload file : sfl) {
					// 判断是否需要保留原来的文件结构
					if (KeepDirStructure) {
						// 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
						// 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
						compress(file, zos, user,
								name + "/" + file.getRealname(), KeepDirStructure);
					} else {
						compress(file, zos, user,
								file.getRealname(), KeepDirStructure);
					}
				}
			}
		}
	}

	/**
	 * 压缩文件夹
	 * @param folderList
	 * @param user
	 * @param out
	 * @param KeepDirStructure
	 * @throws RuntimeException
	 */

	public void toZip(List<Upload> folderList, Users user, ZipOutputStream zos, boolean KeepDirStructure)
			throws RuntimeException {

		try {
			// 压缩文件夹
			for (Upload upload : folderList) {
				compress(upload, zos, user, upload.getRealname(), KeepDirStructure);
			}
		} catch (Exception e) {
			throw new RuntimeException("zip error from ZipUtils", e);
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 压缩文件
	 * @param srcFiles
	 * @param user
	 * @param out
	 * @throws RuntimeException
	 */
	public void toZip(List<String> srcFiles, Users user, ZipOutputStream zos) throws RuntimeException {
		try {
			for (String srcFile : srcFiles) {
				byte[] buf = new byte[BUFFER_SIZE];
				zos.putNextEntry(new ZipEntry(srcFile));
				int len;
				FileInputStream in = new FileInputStream(this.path + "/" + user.getUsername() + "/" + srcFile);
				while ((len = in.read(buf)) != -1) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				in.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("zip error from ZipUtils", e);
		} 
	}

	@Override
	public void toZip(Users user, ZipOutputStream zos, List<Upload> folderList, List<String> fileList) {
		//压缩文件
		toZip(fileList, user, zos);
		//压缩文件夹
		toZip(folderList, user, zos, true);
	}

	@Override
	public int updFolderById(Folder folder) {
		return folderMapper.updateByPrimaryKeySelective(folder);
	}

	@Override
	public RespResult updFolderName(int id, String fname) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("修改失败");
		
		Users user = (Users)SecurityUtils.getSubject().getPrincipal();
		
		//查询文件夹是否存在
		Folder folder = this.selFolderById(id);
		if(folder == null || folder.getUid().intValue()!=user.getId().intValue()) {
			return result;
		}
		//修改文件夹名称
		folder.setFname(fname);
		int i = this.updFolderById(folder);
		if(i > 0) {
			result.setCode(200);
			result.setMsg("修改成功");
		}
		return result;
	}

	@Override
	public RespResult moveFileAndFolder(int fid, int[] folderList, long[] fileList) {
		RespResult result = new RespResult(500,"移动失败",null);
		
		Users user = (Users) SecurityUtils.getSubject().getPrincipal();
		
		if(fid != 0) {
			Folder folder = this.selFolderById(fid);
			if(folder == null || folder.getUid().intValue() != user.getId().intValue()) {
				return result;
			}
		}
		
		int successCount = 0;
		int total = 0;
		//移动文件夹
		if(folderList != null && folderList.length > 0) {
			total += folderList.length;
			for (int id : folderList) {
				Folder f = new Folder();
				f.setId(id);
				f.setFid(fid);
				successCount += this.updFolderById(f);
			}
		}
		//移动文件
		if(fileList != null && fileList.length > 0) {
			total += fileList.length;
			for (long id : fileList) {
				Upload upload = new Upload();
				upload.setId(id);
				upload.setFid(fid);
				successCount += this.uploadServiceImpl.updUploadById(upload);
			}
		}
		
		if(successCount == total) {
			result.setCode(200);
			result.setMsg("移动成功");
			return result;
		}else {
			throw new RuntimeException("移动文件失败");
		}
	}

	@Override
	public int insFloderRetInt(Folder folder) {
		return this.folderMapper.insertSelective(folder);
	}

	@Override
	public List<Folder> selAllFolderByFid(int uid, int fid) {
		List<Folder> retList = new ArrayList<Folder>();
		
		List<Folder> list = this.selFolderByFid(uid, fid);
		retList.addAll(list);
		for (Folder folder : list) {
			retList.addAll(this.selAllFolderByFid(uid, folder.getId()));
		}
		return retList;
	}

	@Override
	public int delAllEmptyFolder(int uid, int fid) {
		List<Folder> folderList = this.selAllFolderByFid(uid, fid);
		
		int i = 0;
		for (Folder folder : folderList) {
			List<Upload> list = this.uploadServiceImpl.selUploadByFid(uid, folder.getId());
			if(list.size()<=0) { //删除空白目录
				i += this.delFolderById(folder.getId(), uid);
			}
		}
		return i;
	}

}
