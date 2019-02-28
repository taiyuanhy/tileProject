package com.uinv.gis.tileProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.JTextArea;

/**
 * GIS相关工具类
 * 
 * @author HY
 *
 */
public class GISUtil {
	public static String bundlesDir = "";
	public static JTextArea textArea;

	/**
	 * 根据瓦片目录获,行列号,级别取瓦片图
	 * 
	 * @param bundlesDir
	 * @param level
	 * @param row
	 * @param col
	 * @return
	 * @throws IOException
	 */
	public static byte[] getTiles(String bundlesDir, int level, int row, int col) throws IOException {
		int size = 128;
		byte[] result = null;
		InputStream isBundle = null;
		InputStream isBundlx = null;
		try {
			String l = "0" + level;
			int lLength = l.length();
			if (lLength > 2) {
				l = l.substring(lLength - 2);
			}
			l = "L" + l;
			int rGroup = size * (row / size);
			String r = Integer.toHexString(rGroup);
			// 行号不足4位补齐4位，如果超过4位不作处理
			if (r.length() < 4) {
				String new_r = "000" + r;
				if (new_r.length() > 4) {
					new_r = new_r.substring(new_r.length() - 4);
				}
				r = "R" + new_r;
			} else {
				r = "R" + r;
			}
			// 列号不足4位补齐4位，如果超过4位不作处理
			int cGroup = size * (col / size);
			String c = Integer.toHexString(cGroup);
			if (c.length() < 4) {
				String new_c = "000" + c;
				if (new_c.length() > 4) {
					new_c = new_c.substring(new_c.length() - 4);
				}
				c = "C" + new_c;
			} else {
				c = "C" + c;
			}

			String bundleBase = bundlesDir + "/" + l + "/" + r + c;
			String bundlxFileName = bundleBase + ".bundlx";
			String bundleFileName = bundleBase + ".bundle";

			int index = size * (col - cGroup) + (row - rGroup);
			// 行列号是整个范围内的，在某个文件中需要先减去前面文件所占有的行列号（都是128的整数）这样就得到在文件中的真是行列号
			// System.out.println(bundlxFileName);
			// System.out.println(bundleFileName);
			// Resource bundlxFileResource =
			// ResourceResolver.getResource(bundlxFileName);
			// Resource bundleFileResource=
			// ResourceResolver.getResource(bundleFileName);
			isBundlx = new FileInputStream(new File(bundlxFileName));
			// isBundlx..Seek(16 + 5 * index,SeekOrigin.Begin);
			isBundlx.skip(16 + 5 * index);
			byte[] buffer = new byte[5];
			isBundlx.read(buffer);
			long offset = (long) (buffer[0] & 0xff) + (long) (buffer[1] & 0xff) * 256
					+ (long) (buffer[2] & 0xff) * 65536 + (long) (buffer[3] & 0xff) * 16777216
					+ (long) (buffer[4] & 0xff) * 4294967296L;

			isBundle = new FileInputStream(new File(bundleFileName));
			// isBundle = new FileInputStream(bundleFileName);
			// isBundle.Seek(offset,SeekOrigin.Begin);
			isBundle.skip(offset);
			byte[] lengthBytes = new byte[4];
			isBundle.read(lengthBytes, 0, 4);
			int length = (int) (lengthBytes[0] & 0xff) + (int) (lengthBytes[1] & 0xff) * 256
					+ (int) (lengthBytes[2] & 0xff) * 65536 + (int) (lengthBytes[3] & 0xff) * 16777216;
			result = new byte[length];
			isBundle.read(result);
		} catch (Exception ex) {
			return null;
		} finally {
			if (isBundle != null) {
				isBundle.close();
				isBundlx.close();
			}
		}
		// System.out.println("level="+level+"row="+row+"col="+col);
		return result;
	}

	public static void combineTileFolder(String folderPath1, String folderPath2) throws IOException {
		File file1 = new File(folderPath1);
		File[] files1 = file1.listFiles();
		String[] names1 = file1.list();
		if (names1 != null) {
			for (int i = 0; i < files1.length; i++) {
				// 切片级数
				String level1 = files1[i].getName();
				if (level1.startsWith("L")) {
					File levelFile = files1[i].getAbsoluteFile();
					File[] levelFiles = levelFile.listFiles();
					for (int j = 0; j < levelFiles.length; j++) {
						String sourceFileName = levelFiles[j].getName();
						String targetFilePath = folderPath2 + File.separator + level1 + File.separator + sourceFileName;
						File targetFile = new File(targetFilePath);
						if (targetFile.exists()) {
							if (levelFiles[j].getAbsolutePath().endsWith("bundle")) {
								combineTile(levelFiles[j].getAbsolutePath(), targetFile.getAbsolutePath());
								System.out.println("合并" + levelFiles[j].getPath() + "到" + targetFile.getPath());
							}
						} else {
							copyFileUsingFileChannels(levelFiles[j], targetFile);
							System.out.println("拷贝" + levelFiles[j].getPath() + "到" + targetFile.getPath());
						}
					}
				}
			}
		}
	}

	public static boolean isItemInArray(String targetValue, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			String s = arr[i];
			if (s.equals(targetValue))
				return true;
		}
		return false;
	}

	private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdir();
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	public static void combineTile(String sorce1, String sorce2) throws IOException {
		// String level =
		// sorce1.substring(sorce1.indexOf("L")+1,sorce1.indexOf("R")-1);
		// String row =
		// sorce1.substring(sorce1.indexOf("R")+1,sorce1.indexOf("C"));
		// String col =
		// sorce1.substring(sorce1.indexOf("C")+1,sorce1.indexOf("bundle")-1);
		long count = 0;
		String bundlxPath1 = sorce1.replace("bundle", "bundlx");
		String bundlxPath2 = sorce2.replace("bundle", "bundlx");
		FileInputStream isBundlx1 = new FileInputStream(bundlxPath1);
		FileInputStream isBundle1 = new FileInputStream(sorce1);
		FileInputStream isBundlx2 = new FileInputStream(bundlxPath2);
		FileInputStream isBundle2 = new FileInputStream(sorce2);
		FileChannel fc1 = isBundlx1.getChannel();
		FileChannel fc1x = isBundle1.getChannel();
		FileChannel fc2 = isBundlx2.getChannel();
		FileChannel fc2x = isBundle2.getChannel();
		byte[][] tile1 = new byte[128 * 128][];
		byte[][] tile2 = new byte[128 * 128][];
		for (int index = 0; index < 128 * 128; index++) {
			fc1.position(0);
			fc1x.position(0);
			fc2.position(0);
			fc2x.position(0);
			isBundlx1.skip(16 + 5 * index);
			byte[] buffer1 = new byte[5];
			isBundlx1.read(buffer1);
			long offset1 = (long) (buffer1[0] & 0xff) + (long) (buffer1[1] & 0xff) * 256
					+ (long) (buffer1[2] & 0xff) * 65536 + (long) (buffer1[3] & 0xff) * 16777216
					+ (long) (buffer1[4] & 0xff) * 4294967296L;
			isBundle1.skip(offset1);
			byte[] lengthBytes1 = new byte[4];
			isBundle1.read(lengthBytes1, 0, 4);
			int length1 = (int) (lengthBytes1[0] & 0xff) + (int) (lengthBytes1[1] & 0xff) * 256
					+ (int) (lengthBytes1[2] & 0xff) * 65536 + (int) (lengthBytes1[3] & 0xff) * 16777216;
			count += length1;
			// System.out.println("length1:"+index+","+length1);
			byte[] result1 = new byte[length1];
			isBundle1.read(result1);
			tile1[index] = result1;

			if (result1.length == 0) {// 如果tile1有该图片，则不读取tile2，直接使用tile1的图，如果没有则使用tile2
				isBundlx2.skip(16 + 5 * index);
				byte[] buffer2 = new byte[5];
				isBundlx2.read(buffer2);
				long offset2 = (long) (buffer2[0] & 0xff) + (long) (buffer2[1] & 0xff) * 256
						+ (long) (buffer2[2] & 0xff) * 65536 + (long) (buffer2[3] & 0xff) * 16777216
						+ (long) (buffer2[4] & 0xff) * 4294967296L;
				isBundle2.skip(offset2);
				byte[] lengthBytes2 = new byte[4];
				isBundle2.read(lengthBytes2, 0, 4);
				int length2 = (int) (lengthBytes2[0] & 0xff) + (int) (lengthBytes2[1] & 0xff) * 256
						+ (int) (lengthBytes2[2] & 0xff) * 65536 + (int) (lengthBytes2[3] & 0xff) * 16777216;
				// System.out.println("length2:"+index+","+length2);
				count += length2;
				// System.out.println("length:"+count);
				byte[] result2 = new byte[length2];
				isBundle2.read(result2);
				tile2[index] = result2;
			} else {
				tile2[index] = new byte[0];
			}
		}
		isBundle2.close();
		isBundlx2.close();
		isBundle1.close();
		isBundlx1.close();
		FileOutputStream fsbundle = new FileOutputStream(new File(sorce2));
		FileOutputStream fsbundlx = new FileOutputStream(new File(bundlxPath2));

		FileChannel fsc = fsbundle.getChannel();
		byte[] bdxBts = new byte[] { 0x03, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x05, 0x00,
				0x00, 0x00 };
		byte[] bdlxEndBts = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00 };
		fsbundlx.write(bdxBts, 0, 16);
		// bundle前60位与内容无关
		fsc.position(60);
		// bundle文件从60-65596位全部都是0
		byte[] bdEmpty = new byte[128 * 128 * 4];
		for (int i = 0; i < 128 * 128 * 4; i++) {
			bdEmpty[i] = 00;
		}
		fsbundle.write(bdEmpty);
		// 从65596位开始，写img的内容，如果img有内容，则追加写入bundle，前4位是img长度，之后是img的内容，写入bundle之后将位置写入bundlx
		// 若无内容，则不写入bundle，但要写bundlx
		for (int index = 0; index < 128 * 128; index++) {
			if (tile1[index].length + tile2[index].length == 0) {// 如果图片为空,不写bundle，仅写bundlx,从60开始记录
				long nullposition = 60 + index * 4;
				byte[] bdlPosBts = convertToByte(nullposition, 10);
				fsbundlx.write(bdlPosBts);
			} else {// 若图片不为空，则写入当前bundle的位置(从65596开始记录的非空部分)
				long offset = fsc.position();
				byte[] bdlPosBts = convertToByte(offset, 10);
				fsbundlx.write(bdlPosBts);
				long tilelength = 0;
				if (tile1[index].length != 0) {
					tilelength = tile1[index].length;
					byte[] lengthByte = convertToByte(tilelength, 8);
					fsbundle.write(lengthByte);
					fsbundle.write(tile1[index]);
				} else if (tile2[index].length != 0) {
					tilelength = tile2[index].length;
					byte[] lengthByte = convertToByte(tilelength, 8);
					fsbundle.write(lengthByte);
					fsbundle.write(tile2[index]);
				}
			}
		}
		fsbundlx.write(bdlxEndBts);
		fsbundlx.close();
		fsbundle.close();
	}

	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	public static String decToHex(long dec, int length) {
		String hex = "";
		int count = 0;
		while (dec != 0) {
			String h = Long.toString(dec & 0xff, 16);
			if ((h.length() & 0x01) == 1)
				h = '0' + h;
			hex += h;
			dec = dec >> 8;
			count++;
		}
		if (hex.length() < 10) {
			hex = hex + "0000000000";
			hex = hex.substring(0, length);
		}
		return hex;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte[] convertToByte(long s, int length) {
		String hexPostion = decToHex(s, length);
		byte[] bdlPosBts = hexStringToBytes(hexPostion);
		return bdlPosBts;
	}

	public static boolean isNumeric(String str) {
		final String number = "0123456789";
		for (int i = 0; i < str.length(); i++) {
			if (number.indexOf(str.charAt(i)) == -1) {
				return false;
			}
		}
		return true;
	}

	public static void generateBundleFile(String path) throws IOException {
		File file = new File(path);
		String suffix = getSuffix(file);
		File[] files = file.listFiles();
		String[] names = file.list();
		int m_packetSize = 128;
		if (names != null) {
			for (int i = 0; i < files.length; i++) {
				// 切片级数
				String level = files[i].getName();
				if (isNumeric(level)) {
					File coldir = files[i].getAbsoluteFile();
					File[] cols = coldir.listFiles();
					int mincol = Integer.MAX_VALUE;
					int maxcol = Integer.MIN_VALUE;
					for (int j = 0; j < cols.length; j++) {
						int colnunm = Integer.parseInt(cols[j].getName());
						if (colnunm < mincol)
							mincol = colnunm;
						if (colnunm > maxcol)
							maxcol = colnunm;
					}
					int minrow = Integer.MAX_VALUE;
					int maxrow = Integer.MIN_VALUE;
					for (int j = mincol; j <= maxcol; j++) {
						String rowdirPath = files[i].getAbsolutePath() + "/" + j;
						File rowdirDir = new File(rowdirPath);
						File[] rows = rowdirDir.listFiles();
						if (rows != null) {// 文件夹不为空
							for (int k = 0; k < rows.length; k++) {
								int rownum = Integer
										.parseInt(rows[k].getName().substring(0, rows[k].getName().lastIndexOf(".")));
								if (rownum < minrow)
									minrow = rownum;
								if (rownum > maxrow)
									maxrow = rownum;
							}
						}
					}
					// 左上角的bundle文件开始行列号
					long minBundleRow = m_packetSize * (minrow / m_packetSize);
					long minBundleCol = m_packetSize * (mincol / m_packetSize);
					// 右下角（最后一个）的bundle文件开始行列号
					long maxBundleRow = m_packetSize * (maxrow / m_packetSize);
					long maxBundleCol = m_packetSize * (maxcol / m_packetSize);
					System.out.println("第" + level + "级--bundle最小行数:" + minBundleRow + ",最小列数:" + minBundleCol);
					System.out.println("第" + level + "级--bundle最大行数:" + maxBundleRow + ",最大列数:" + maxBundleCol);
					System.out.println("第" + level + "级--bundle共:" + ((maxBundleRow - minBundleRow) / 128 + 1) + "行,"
							+ ((maxBundleCol - minBundleCol) / 128 + 1) + "列.");
					// 该级别下全部bundle文件
					for (long j = minBundleCol; j <= maxBundleCol; j += 128) {
						for (long j2 = minBundleRow; j2 <= maxBundleRow; j2 += 128) {
							String colhex = Long.toHexString(j);
							// 长度小于4,补零到4位
							if (colhex.length() < 4) {
								colhex = "0000" + colhex;
								colhex = colhex.substring(colhex.length() - 4);
							}
							String rowhex = Long.toHexString(j2);
							if (rowhex.length() < 4) {
								rowhex = "0000" + rowhex;
								rowhex = rowhex.substring(rowhex.length() - 4);
							}
							String filename = "R" + rowhex + "C" + colhex;
							System.out.println("第" + level + "级:" + filename);
							generateBundleFromImage(path, Integer.parseInt(level), j, j2, filename, suffix);
						}
					}
				}
			}
		}
	}

	public static void generateBundleFromImage(String path, int level, long col, long row, String filename,
			String suffix) throws IOException {
		String bundlePath = "bundle";
	    String l = "0" + level;
	    int lLength = l.length();
	    if (lLength > 2) {
	      l = l.substring(lLength - 2);
	    }
	    l = "L" + l;
	    String bundlepath = path + File.separator + bundlePath + File.separator + l + File.separator + filename + ".bundle";
	    String bundlxpath = path + File.separator + bundlePath + File.separator + l + File.separator + filename + ".bundlx";
	    if (!new File(bundlepath).exists()) {
	      new File(path + File.separator + bundlePath + File.separator + l).mkdirs();
	    }
		FileOutputStream fsbundle = new FileOutputStream(bundlepath);
		FileOutputStream fsbundlx = new FileOutputStream(bundlxpath);

		FileChannel fsc = fsbundle.getChannel();
		byte[] bdxBts = new byte[] { 0x03, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x05, 0x00,
				0x00, 0x00 };
		byte[] bdlxEndBts = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00 };
		fsbundlx.write(bdxBts, 0, 16);
		// bundle前60位与内容无关
		fsc.position(60);
		// bundle文件从60-65596位全部都是0
		byte[] bdEmpty = new byte[128 * 128 * 4];
		for (int i = 0; i < 128 * 128 * 4; i++) {
			bdEmpty[i] = 00;
		}
		fsbundle.write(bdEmpty);
		// 从65596位开始，写img的内容，如果img有内容，则追加写入bundle，前4位是img长度，之后是img的内容，写入bundle之后将位置写入bundlx
		// 若无内容，则不写入bundle，但要写bundlx
		for (int i = 0; i < 128 * 128; i++) {
			long currentCol = col + i / 128;
			long currentRow = i - (i / 128) * 128 + row;
			String picpath = path + "\\" + level + "\\" + currentCol + "\\" + currentRow + "." + suffix;
			File file = new File(picpath);
			if (file.exists()) {
				long offset = fsc.position();
				byte[] bdlPosBts = convertToByte(offset, 10);
				fsbundlx.write(bdlPosBts);
				FileInputStream fis = new FileInputStream(file);
				byte[] img_content = new byte[(int) file.length()];
				fis.read(img_content);
				fis.close();
				long tilelength = img_content.length;
				byte[] lengthByte = convertToByte(tilelength, 8);
				fsbundle.write(lengthByte);
				fsbundle.write(img_content);
				// System.out.println(path+"\\"+currentCol+"\\"+currentRow+".jpg");
			} else {
				long nullposition = 60 + i * 4;
				byte[] bdlPosBts = convertToByte(nullposition, 10);
				fsbundlx.write(bdlPosBts);
			}
		}
		fsbundlx.write(bdlxEndBts);
		fsbundlx.close();
		fsbundle.close();
	}

	public static String getSuffix(File file) {
		String suffix = "jpg";
		// 执行操作
		File image = file.listFiles()[0].listFiles()[0].listFiles()[0];
		String imageName = image.getName();
		suffix = imageName.substring(imageName.lastIndexOf(".") + 1);
		return suffix;
	}

	public static void main(String args[]) throws Exception {
		// combineTile("F:\\tiles\\_alllayers\\test\\L17\\Rc580Ca000(1).bundle","F:\\tiles\\_alllayers\\test\\L17\\Rc580Ca000(2).bundle");
		generateBundleFile("E:\\北京0-12\\googlemaps\\satellite");
		// combineTileFolder("E:\\哈尔滨\\googlemaps\\satellite_en","E:\\tiles\\satellite");
	}
	private static class System
	{
	  private static class out
	  {
	    private static void println(String a)
	    {
	      textArea.append(a);
	      textArea.append("\r\n");
          textArea.setCaretPosition(textArea.getText().length());
	    }
	  }
	}
}

