package com.umi.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

 
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageUtils {

	private static String[] imageType = {".bmp",".jpg",".jpeg",".png",".tif",
			".gif",".pcx",".tga",".exif",".fpx",".svg",
			".psd",".cdr",".pcd",".dxf",".ufo",".eps",
			".ai",".raw",".WMF",".webp",".avif"};
	
	/**
	 * 根据图片名称判断是否是图片
	 * @param imageName
	 * @return
	 */
	public static boolean isImage(String imageName) {
		if(null==imageName||imageName.equals("")) {
			return false;
		}
		
		boolean flag = false;
		for (String type : imageType) {
			if(imageName.endsWith(type)) {
				flag = true;
				break;
			}
		}
		
		return flag;
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
			
			// 获得源图片的宽高存入数组中
			int[] results = getImgWidthHeight(srcfile);
			// 如果比例不为空则说明是按比例压缩
			if (rate != null && rate > 0) {
				if (results == null || results[0] == 0 || results[1] == 0) {
					return;
				} else {
					// 按比例缩放或扩大图片大小，将浮点型转为整型
					widthdist = (int) (results[0] * rate);
					heightdist = (int) (results[1] * rate);
				}
			}
			
			//只给定一个参数 另外一个参数自动缩放
			if(widthdist<=0 || heightdist<=0) {
				if(heightdist>0) {
					widthdist = (int) ((results[0]/(results[1]*1.0))*heightdist);
				}
				if(widthdist>0) {
					heightdist = (int) ((results[1]/(results[0]*1.0))*widthdist);
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
			try {
				ImageUtils.copyImageToSolue(imgsrc, imgdist);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ef.printStackTrace();
		}
	}

	
	public static int[] getWidthHeight(File file) throws IOException {
		FileInputStream is =  new FileInputStream(file);
        byte[] bytes = new byte[30];
 
        is.read(bytes,0,bytes.length);
 
       int width = ((int) bytes[27] & 0xff)<<8 | ((int) bytes[26] & 0xff);
       int height = ((int) bytes[29] & 0xff)<<8 | ((int) bytes[28] & 0xff);
       return new int[] {width, height};
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
			src = javax.imageio.ImageIO.read(is);
			System.out.println(file.getPath());
			System.out.println("src:"+src);
			result[0] = src.getWidth(null); // 得到源图片宽
			result[1] = src.getHeight(null);// 得到源图片高
			is.close(); // 关闭输入流
		} catch (Exception ef) {
			ef.printStackTrace();
		}

		return result;
	}

	//把图片拷贝到缩略图文件夹下
	public static void copyImageToSolue(String path, String path2) throws IOException {
		FileInputStream srcFis = new FileInputStream(path);
		FileOutputStream fos = new FileOutputStream(path2);
		byte[] temp = new byte[srcFis.available()];
		srcFis.read(temp);
		fos.write(temp);
		srcFis.close();
		fos.close();
	}
}
