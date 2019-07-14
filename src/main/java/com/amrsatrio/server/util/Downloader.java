package com.amrsatrio.server.util;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
	public static Thread download(final File saveDir, final URL url, final long maxSize, final DownloadListener l, final Player p) {
		Thread th = new Thread(() -> {
			HttpURLConnection httpurlconnection = null;
			InputStream inputstream = null;
			OutputStream outputstream = null;
			File saveFile = null;
			try {
				l.onStarted();
				byte[] abyte = new byte[4096];
				httpurlconnection = (HttpURLConnection) url.openConnection();
				httpurlconnection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
				httpurlconnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
				String raw = httpurlconnection.getHeaderField("Content-Disposition");
				String fn;
				if (raw != null && raw.contains("=")) {
					fn = raw.split("=")[1].replaceAll("\"", "");
				} else {
					String[] ff = url.getFile().split("/");
					fn = ff[ff.length - 1];
					if (fn.isEmpty()) {
						fn = "download";
					}
				}
				saveFile = Utils.createUniqueCopyName(saveDir, fn);
				System.out.println("Gonna write to " + saveFile.getAbsolutePath());
				long f = 0;
				inputstream = httpurlconnection.getInputStream();
				long i = httpurlconnection.getContentLengthLong();
				if (saveFile.exists()) {
					throw new IOException("That file already exists!");
				} else if (saveFile.getParentFile() != null) {
					saveFile.getParentFile().mkdirs();
				}
				outputstream = new DataOutputStream(new FileOutputStream(saveFile));
				if (maxSize > 0 && i > maxSize) {
					throw new IOException("Filesize is bigger than maximum allowed (file is " + i + ", limit is " + maxSize + ")");
				}
				int k;
				while ((k = inputstream.read(abyte)) >= 0) {
					f += k;
					l.onProgress(f, i, saveFile);
					if (maxSize > 0 && f > maxSize) {
						throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + maxSize + ")");
					}
					if (Thread.interrupted()) {
						l.onInterrupted();
						return;
					}
					outputstream.write(abyte, 0, k);
				}
				l.onCompleted(saveFile);
			} catch (Throwable throwable) {
				try {
					if (httpurlconnection != null) {
						InputStream inputstream1 = httpurlconnection.getErrorStream();
						System.err.println(IOUtils.toString(inputstream1));
						inputstream1.close();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				if (saveFile != null) {
					saveFile.delete();
				}
				l.onInterrupted();
				Utils.broke(throwable);
			} finally {
				IOUtils.closeQuietly(inputstream);
				IOUtils.closeQuietly(outputstream);
			}
		});
		th.start();
		return th;
	}

	public interface DownloadListener {
		void onCompleted(File a);

		void onInterrupted();

		void onProgress(long a, long b, File c);

		void onStarted();
	}
}
