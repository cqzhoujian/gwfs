package com.supermap.gwfs.clipper.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class FTPClientFactory {
	protected static Logger logger = Logger.getLogger(FTPClientFactory.class);
	private static String CONFIG_FILE_LOCATION = "config/clipper/ftpconfig.properties";
	private static String userName; // FTP 登录用户名
	private static String password; // FTP 登录密码
	private static String ip; // FTP 服务器地址IP地址
	private static int port; // FTP 端口
	private static Properties property = null; // 属性集
	private static String configFile = CONFIG_FILE_LOCATION; // 配置文件的路径名
	// private static FTPClient ftpClient = null; // FTP 客户端代理
	private static final ThreadLocal<FTPClient> threadLocal = new ThreadLocal<FTPClient>();

	static {
		try {
			setArg(configFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/** 
	* 下载文件 
	* 
	* @param remoteFileName--服务器上的文件名 
	* @param localFileName--本地文件名 
	* @return true 下载成功，false 下载失败 
	*/
	public static boolean loadFile(String remoteFileName, String localFileName) {
		FTPClient ftpClient = getFtpClient();
		boolean flag = true;

		// 下载文件
		BufferedOutputStream buffOut = null;
		try {
			connectServer(ftpClient);
			// 新建下载文件夹目录
			createLocalFolder(localFileName);

			buffOut = new BufferedOutputStream(new FileOutputStream(localFileName));
			flag = ftpClient.retrieveFile(remoteFileName, buffOut);
		} catch (Exception e) {
			logger.error("文件下载失败！", e);
		} finally {
			try {
				if (buffOut != null)
					buffOut.close();
				if (ftpClient != null)
					closeConnect();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return flag;
	}

	private static boolean createLocalFolder(String localFileName) {
		String directory = localFileName.substring(0, localFileName.lastIndexOf("/"));
		String fileName = localFileName.substring(localFileName.lastIndexOf("/") + 1);
		boolean flag = true;
		// 在内存中创建一个文件对象，：此时还没有在硬盘对应目录下创建实实在在的文件
		File f = new File(directory, fileName);
		if (f.exists()) {

		} else {
			// 先创建文件所在的目录
			flag = f.getParentFile().mkdirs();

			// try {
			// // 创建新文件
			// f.createNewFile();
			// } catch (IOException e) {
			// logger.error("创建新文件时出现了错误。。。", e);
			// }
		}
		return flag;

	}

	/**
	* 创建远程服务器目录
	* 
	* @param remote 远程服务器文件绝对路径
	* @return 目录创建是否成功
	*/
	public static synchronized boolean createDirecroty(String remote, FTPClient ftpClient) {
		try {
			if (ftpClient.changeWorkingDirectory(remote)) {
				// 文件夹存在
			} else {
				// 文件夹不存在,创建目录
				String[] paths = remote.split("/");
				StringBuffer path = new StringBuffer();
				for (String string : paths) {
					path.append(string + "/");
					ftpClient.makeDirectory(path.toString());
				}
				logger.info("创建目录-" + remote);
			}
		} catch (IOException e) {
			logger.error("创建目录-" + remote + "失败");
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/** 
	* 上传单个文件
	* @return true 上传成功，false 上传失败 
	*/
	public static boolean uploadFile(File localFile, String remoteFileName) {
		FTPClient ftpClient = getFtpClient();

		boolean flag = true;
		InputStream input = null;
		try {
			flag = connectServer(ftpClient);
			flag = createDirecroty(remoteFileName.substring(0, remoteFileName.lastIndexOf("/")), ftpClient);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

			input = new FileInputStream(localFile);
			flag = ftpClient.storeFile(remoteFileName, input);
			if (flag) {
				logger.info("上传文件-" + localFile.getAbsolutePath() + "成功！");
			} else {
				logger.info("上传文件-" + localFile.getAbsolutePath() + "失败！");
			}

		} catch (IOException e) {
			logger.info("本地文件-" + localFile.getAbsolutePath() + "上传失败！", e);
		} finally {
			try {
				if (input != null)
					input.close();

				if (ftpClient != null)
					closeConnect();

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return flag;
	}

	public static boolean uploadFile(File[] files, String remotePath) {
		boolean flag = true;
		for (File file : files) {
			String remoteFileName = remotePath + "/" + file.getName();
			flag = uploadFile(file, remoteFileName);
			if (!flag)
				return false;

		}
		return true;
	}

	public static FTPClient getFtpClient() {
		FTPClient ftpClient = threadLocal.get();
		// 呵呵
		if (ftpClient == null || !ftpClient.isAvailable()) {
			ftpClient = new FTPClient();
			threadLocal.set(ftpClient);
		}
		return ftpClient;
	}

	/** 
	 * 关闭连接 
	 */
	public static void closeConnect() {
		FTPClient ftpClient = threadLocal.get();
		threadLocal.set(null);
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/** 
	 * 连接到服务器 
	 * 
	 * @return true 连接服务器成功，false 连接服务器失败 
	 * @throws Exception 
	 */
	public static boolean connectServer(FTPClient ftpClient) {
		boolean flag = true;
		int reply;
		try {
			// ftpClient = new FTPClient();
			// ftpClient.setControlEncoding("GBK");
			ftpClient.configure(getFtpConfig());
			ftpClient.connect(ip);
			ftpClient.login(userName, password);

			ftpClient.configure(new FTPClientConfig(ftpClient.getSystemType()));

			ftpClient.setDefaultPort(port);
			// 每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
			ftpClient.enterLocalPassiveMode();

			reply = ftpClient.getReplyCode();
			ftpClient.setDataTimeout(120000);

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				logger.error("登录FTP服务器失败,请检查![Server:" + ip + "、" + "User:" + userName + "、" + "Password:" + password);
				logger.debug("FTP 服务拒绝连接！");
				flag = false;
				// 释放空间
				ftpClient = null;
			}
		} catch (SocketException e) {
			flag = false;
			logger.error("登录ftp服务器 " + ip + " 失败,连接超时！" + e.getMessage(), e);
		} catch (IOException e) {
			flag = false;
			logger.error("登录ftp服务器 " + ip + " 失败，FTP服务器无法打开！" + e.getMessage(), e);
		}
		return flag;
	}

	/** 
	 * 设置参数 
	 * 
	 * @param configFile --参数的配置文件 
	 */
	private static void setArg(String configFile) {
		property = new Properties();
		BufferedInputStream inBuff = null;
		try {
			File file = new File(configFile);
			inBuff = new BufferedInputStream(new FileInputStream(file));
			property.load(inBuff);
			userName = property.getProperty("username");
			password = property.getProperty("password");
			ip = property.getProperty("ip");
			port = Integer.parseInt(property.getProperty("port"));
		} catch (FileNotFoundException e1) {
			System.out.println("配置文件 " + configFile + " 不存在！");
		} catch (IOException e) {
			System.out.println("配置文件 " + configFile + " 无法读取！");
		}
	}

	/** 
	 * 设置FTP客服端的配置--一般可以不设置 
	 * 
	 * @return ftpConfig 
	 */
	private static FTPClientConfig getFtpConfig() {
		FTPClientConfig ftpConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
		ftpConfig.setServerLanguageCode(FTP.DEFAULT_CONTROL_ENCODING);
		return ftpConfig;

	}
}
