package org.szernex.yabm.util;

import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileHelper
{
	public static class BackupFileFilter implements FileFilter
	{
		private String nameFilter = "";

		public BackupFileFilter()
		{

		}

		public BackupFileFilter(String name_filter)
		{
			nameFilter = name_filter;
		}

		@Override
		public boolean accept(File file)
		{
			return (file.isFile() && file.getName().startsWith(nameFilter));
		}
	}

	public static final String TIMESTAMP_FORMAT = "y-MM-dd_HH-mm-ss";

	public static String getArchiveFileName(String prefix, boolean includetimestamp)
	{
		String output;

		prefix = prefix.replaceAll(" ", "_");

		if (MinecraftServer.getServer().isDedicatedServer())
		{
			output = String.format("%s_%s", prefix, (includetimestamp ? getFormattedTime(System.currentTimeMillis(), TIMESTAMP_FORMAT) : ""));
		}
		else
		{
			output = String.format("%s_%s_%s", prefix, MinecraftServer.getServer().getFolderName().replaceAll(" ", "_"), (includetimestamp ? getFormattedTime(System.currentTimeMillis(), TIMESTAMP_FORMAT) : ""));
		}

		return output;
	}

	public static String getFormattedTime(long timestamp, String format)
	{
		if (format == null)
		{
			format = TIMESTAMP_FORMAT;
		}

		SimpleDateFormat dateformat = new SimpleDateFormat(format);

		return dateformat.format(new Date(timestamp));
	}

	public static Set<File> getDirectoryContents(File root)
	{
		Set<File> output = new HashSet<File>();
		File[] files;

		if (root == null)
		{
			return null;
		}

		files = root.listFiles();

		if (files == null)
		{
			return null;
		}

		for (File f : files)
		{
			if (f.exists())
			{
				if (f.isDirectory())
				{
					output.addAll(getDirectoryContents(f));
				}
				else
				{
					output.add(f);
				}
			}
		}

		return output;
	}

	public static Set<File> getFileList(String[] source_list) throws IOException
	{
		Set<File> output = new HashSet<File>();

		for (String f : source_list)
		{
			File file = new File(f).getCanonicalFile();

			if (file.exists())
			{
				if (file.isDirectory())
				{
					output.addAll(FileHelper.getDirectoryContents(file));
				}
				else
				{
					output.add(file);
				}
			}
		}

		return output;
	}

	public static boolean createZipArchive(File target_file, Set<File> source_files, int compression_level) throws IOException
	{
		if (target_file.exists())
		{
			LogHelper.error("Cannot create archive %s: file already exists", target_file);
			return false;
		}

		FileOutputStream output = new FileOutputStream(target_file);
		ZipOutputStream zip = new ZipOutputStream(output);

		zip.setMethod(ZipOutputStream.DEFLATED);

		if (compression_level >= 0 && compression_level <= 9)
		{
			zip.setLevel(compression_level);
		}

		for (File f : source_files)
		{
			if (!f.exists())
			{
				continue;
			}

			LogHelper.debug("Adding to archive: " + f);
			zip.putNextEntry(new ZipEntry(f.toString()));

			byte[] bytes = new byte[1024];
			int length;
			FileInputStream input = new FileInputStream(f);

			while ((length = input.read(bytes)) >= 0)
			{
				zip.write(bytes, 0, length);
			}

			zip.closeEntry();
			input.close();
		}

		zip.close();
		output.close();

		return true;
	}
}
