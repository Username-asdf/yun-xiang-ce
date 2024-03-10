package com.umi.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;

import com.alibaba.fastjson.JSON;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 机器学习 
 * 图像学习
 * 图像识别
 * 图片更改像素
 * 工具类
 */
@SuppressWarnings("restriction")
public class ImageUtils1 {

	// 创建一个静态单例配置模板
	public static TempleConfig templeConfig = new TempleConfig();
	public static int imgWidth = 3024;
	public static int imgHeight = 4032;

	/**
	 * 多线程机器学习 精准模式学习 有多少分类创建多少线程
	 * 
	 * @param directory         训练图片的目录
	 * @param width             图片像素宽度
	 * @param height            图片像素高度
	 * @param classificationNub 分类数 不能为0
	 * @param imgList			需要训练图片的目录列表  和directory只能存在一个 imgList优先
	 * @return 模型json字符串
	 * @throws Exception
	 */
	public static String machineLearnForModelStr(String directory, int width, int height, int classificationNub,
			List<String> imgList) throws Exception {
		String model = JSON
				.toJSONString(machineLearnForModelObkect(directory, width, height, classificationNub, imgList));
		return model;
	}

	/**
	 * 多线程机器学习 精准模式学习 有多少分类创建多少线程
	 * 
	 * @param directory         训练图片的目录
	 * @param width             图片像素宽度
	 * @param height            图片像素高度
	 * @param classificationNub 分类数 不能为0
	 * @return 模型对象
	 * @throws Exception
	 */
	public static ModelParameter machineLearnForModelObkect(String directory, int width, int height,
			int classificationNub, List<String> imgList) throws Exception {
		// 进度条
		final ProcessBar bar = new ProcessBar("第一阶段学习");
		if (null == imgList && null != directory) {
			// 处理图片
			imgList = convertImage(directory, width, height, 0f);
			if (imgList.size() != classificationNub) {
				throw new Exception("需要训练的目录和classificationNum不匹配");
			}
			for (String dir : imgList) {
				File tempFile = new File(dir);
				if(tempFile.isDirectory()) {
					bar.setTotatlNum(bar.getTotatlNum() + tempFile.list().length);
				}
			}
		}else {
			for (String imgPath : imgList) {
				File tempFile = new File(imgPath);
				if(tempFile.isDirectory()) {
					bar.setTotatlNum(bar.getTotatlNum() + tempFile.list().length);
				}
			}
		}
		
		// 创建图片解析类
		Picture picture = new Picture();
		// 第三个参数和第四个参数分别是训练图片的宽和高，为保证训练的稳定性请保证训练图片大小的一致性
		ImageUtils1.templeConfig.init(StudyPattern.Accuracy_Pattern, true, width, height, classificationNub);// 最后一个参数：训练个数
		// 将配置模板类作为构造塞入计算类
		Operation operation = new Operation(ImageUtils1.templeConfig);
		if (classificationNub <= 0) {
			throw new Exception("classificationNub 必须为正整数");
		}

		List<Thread> tList = new ArrayList<Thread>();
		// 开启多线程
		for (int i = 0; i < classificationNub; i++) {
			String currentPath = imgList.get(i);
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					// 遍历子训练目录进行训练
					File file = new File(currentPath);
					String[] turnImgList = file.list();
					int ID = Integer.parseInt(currentPath.substring(currentPath.lastIndexOf("\\") + 1));
					for (int j = 0; j < turnImgList.length; j++) {
						// 一阶段 循环读取不同的图片
						try {
							// 读取本地URL地址图片,并转化成矩阵
							Matrix a = picture.getImageMatrixByLocal(currentPath + "\\" + turnImgList[j]);
							// 矩阵塞入运算类进行学习，第一个参数是图片矩阵，第二个参数是图片分类标注ID，第三个参数是第一次学习固定false
							operation.learning(a, ID, false);

							bar.setCurrentNum(bar.getCurrentNum() + 1);
							bar.printCurrentProcess();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				}
			});
			t.start();
			tList.add(t);
		}
		for (Thread thread : tList) {
			thread.join();
		}

		bar.println();
		bar.setCurrentNum(0);
		bar.setMsg("第二阶段学习");

		tList.clear();
		// 第二阶段学习
		for (int i = 0; i < classificationNub; i++) {
			String currentPath = imgList.get(i);

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					File file = new File(currentPath);
					String[] turnImgList = file.list();
					int ID = Integer.parseInt(currentPath.substring(currentPath.lastIndexOf("\\") + 1));
					for (int j = 0; j < turnImgList.length; j++) {
						// 读取本地URL地址图片,并转化成矩阵
						try {
							Matrix a = picture.getImageMatrixByLocal(currentPath + "\\" + turnImgList[j]);
							// 将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
							// 第二次学习的时候，第三个参数必须是 true
							operation.learning(a, ID, true);

							bar.setCurrentNum(bar.getCurrentNum() + 1);
							bar.printCurrentProcess();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			t.start();
			tList.add(t);
		}
		for (Thread thread : tList) {
			thread.join();
		}
		

		ImageUtils1.templeConfig.finishStudy();// 结束学习 第三阶段学习

		bar.println();
		bar.setMsg("第三阶段学习");
		bar.setTotatlNum(1);
		bar.setCurrentNum(1);
		bar.printCurrentProcess();
		bar.println();

		// 获取学习结束的模型参数,并将model保存数据库
		ModelParameter modelParameter = ImageUtils1.templeConfig.getModel();
		return modelParameter;
	}

	/**
	 * 
	 * @param imgPath 图片的全路径
	 * @param modelJson 模型的json字符串 为null时不注入模型
	 * @return 识别图片的类别
	 * @throws Exception 
	 */
	public static int toSeeByModleJson(String imgPath, String modelJson) throws Exception {
		if(null != modelJson) {
			// 从数据库中读取学习的模型结果，反序列为ModelParameter
			ModelParameter modelParameter = JSON.parseObject(modelJson, ModelParameter.class);
			// 将模型数据注入配置模板类
			ImageUtils1.templeConfig.insertModel(modelParameter);
		}
		return toSeeByModelObject(imgPath, null);
	}
	
	/**
	 * 
	 * @param imgStream 图片的输入流
	 * @param modelJson 模型的json字符串 为null时不注入模型
	 * @return 识别图片的类别
	 * @throws Exception 
	 */
	public static int toSeeByModleJson(InputStream imgStream, String modelJson) throws Exception {
		if(null != modelJson) {
			// 从数据库中读取学习的模型结果，反序列为ModelParameter
			ModelParameter modelParameter = JSON.parseObject(modelJson, ModelParameter.class);
			// 将模型数据注入配置模板类
			ImageUtils1.templeConfig.insertModel(modelParameter);
		}
		return toSeeByModelObject(imgStream, null);
	}
	
	/**
	 * 
	 * @param imgPath 图片的全路径
	 * @param modelObject 模型的ModelParameter对象 为null时不注入模型
	 * @return 识别图片的类别
	 * @throws Exception 
	 */
	public static int toSeeByModelObject(String imgPath, ModelParameter modelParameter) throws Exception {
		if(null != modelParameter) {
			ImageUtils1.templeConfig.insertModel(modelParameter);
		}
		Picture p = new Picture();
		// 将配置模板类作为构造塞入计算类
		Operation operation = new Operation(ImageUtils1.templeConfig);
		return operation.toSee(p.getImageMatrixByLocal(imgPath));
	}
	
	/**
	 * 
	 * @param imgStream 图片的输入流
	 * @param modelObject 模型的ModelParameter对象 为null时不注入模型
	 * @return 识别图片的类别
	 * @throws Exception 
	 */
	public static int toSeeByModelObject(InputStream imgStream, ModelParameter modelParameter) throws Exception {
		if(null != modelParameter) {
			ImageUtils1.templeConfig.insertModel(modelParameter);
		}
		Picture p = new Picture();
		// 将配置模板类作为构造塞入计算类
		Operation operation = new Operation(ImageUtils1.templeConfig);
		return operation.toSee(p.getImageMatrixByIo(imgStream));
	}
	
	/**
	 * 指定图片宽度和高度或压缩比例对图片进行压缩
	 * 
	 * @param imgsrc     源图片地址
	 * @param imgdist    目标图片地址
	 * @param widthdist  压缩后图片的宽度
	 * @param heightdist 压缩后图片的高度
	 * @param rate       压缩的比例
	 */
	public static void reduceImg(String imgsrc, String imgdist, int widthdist, int heightdist, Float rate) {
		try {
			File srcfile = new File(imgsrc);
			// 检查图片文件是否存在
			if (!srcfile.exists()) {
				System.out.println("文件不存在");
			}
			// 如果比例不为空则说明是按比例压缩
			if (rate != null && rate > 0) {
				// 获得源图片的宽高存入数组中
				int[] results = getImgWidthHeight(srcfile);
				if (results == null || results[0] == 0 || results[1] == 0) {
					return;
				} else {
					// 按比例缩放或扩大图片大小，将浮点型转为整型
					widthdist = (int) (results[0] * rate);
					heightdist = (int) (results[1] * rate);
				}
			}
			// 开始读取文件并进行压缩
			Image src = ImageIO.read(srcfile);

			// 构造一个类型为预定义图像类型之一的 BufferedImage
			BufferedImage tag = new BufferedImage((int) widthdist, (int) heightdist, BufferedImage.TYPE_INT_RGB);

			// 绘制图像 getScaledInstance表示创建此图像的缩放版本，返回一个新的缩放版本Image,按指定的width,height呈现图像
			// Image.SCALE_SMOOTH,选择图像平滑度比缩放速度具有更高优先级的图像缩放算法。
			tag.getGraphics().drawImage(src.getScaledInstance(widthdist, heightdist, Image.SCALE_SMOOTH), 0, 0, null);

			// 创建文件输出流
			FileOutputStream out = new FileOutputStream(imgdist);
			// 将图片按JPEG压缩，保存到out中
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(tag);
			// 关闭文件输出流
			out.close();
		} catch (Exception ef) {
			ef.printStackTrace();
		}
	}

	/**
	 * 获取图片宽度和高度
	 * 
	 * @param 图片路径
	 * @return 返回图片的宽度
	 */
	public static int[] getImgWidthHeight(File file) {
		InputStream is = null;
		BufferedImage src = null;
		int result[] = { 0, 0 };
		try {
			// 获得文件输入流
			is = new FileInputStream(file);
			// 从流里将图片写入缓冲图片区
			src = ImageIO.read(is);
			result[0] = src.getWidth(null); // 得到源图片宽
			result[1] = src.getHeight(null);// 得到源图片高
			is.close(); // 关闭输入流
		} catch (Exception ef) {
			ef.printStackTrace();
		}

		return result;
	}

	/**
	 * 将图片处理成指定像素大小的图片 转换后的图片保存在 当前目录下的convertImage文件夹下
	 * 
	 * @param directory
	 * @param widthdist
	 * @param heightdist
	 * @param rate
	 * @return 处理后图片文件夹列表
	 * @throws Exception
	 */
	public static List<String> convertImage(String directory, int widthdist, int heightdist, Float rate)
			throws Exception {
		File file = new File(directory);
		if (!file.isDirectory()) {
			throw new Exception("给定的路径不是一个文件夹");
		}

		List<String> retImageDirs = new ArrayList<String>();
		String descPath = file.getAbsolutePath() + "/convertImage/";

		String[] firstDirList = file.list();
		List<Thread> tList = new ArrayList<Thread>();

		// 进度条
		final ProcessBar bar = new ProcessBar("图片处理进度");
		// 读取文件总个数
		for (int i = 0; i < firstDirList.length; i++) {
			File tempFile = new File(file.getAbsolutePath() + "/" + file.list()[i]);
			if(tempFile.isDirectory()) {
				bar.setTotatlNum(bar.getTotatlNum() + tempFile.list().length);
			}
		}

		for (int i = 0; i < firstDirList.length; i++) {
			if (file.list()[i].equals("convertImage")) {
				break;
			}

			File srcDir = new File(file.getAbsolutePath() + "/" + file.list()[i]);
			if (srcDir.isFile()) {
				continue;
			}
			String[] imgList = srcDir.list();
			if (imgList.length <= 0) {
				continue;
			}
			File descDir = new File(descPath + file.list()[i]);
			if (!descDir.exists()) {
				descDir.mkdirs();
			}
			retImageDirs.add(descDir.getAbsolutePath());

			Thread t1 = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int j = 0; j < imgList.length; j++) {
						reduceImg(srcDir.getAbsolutePath() + "/" + imgList[j],
								descDir.getAbsolutePath() + "/" + imgList[j], widthdist, heightdist, rate);
						// 设置当前进度
						bar.setCurrentNum(bar.getCurrentNum() + 1);
						bar.printCurrentProcess();
					}
				}
			});
			tList.add(t1);
			t1.start();
		}

		for (Thread thread : tList) {
			thread.join();
		}
		
		bar.println();
		return retImageDirs;
	}
}

