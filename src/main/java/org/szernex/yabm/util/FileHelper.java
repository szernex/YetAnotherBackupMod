package org.szernex.yabm.util;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileHelper
{
	private static FileFilter fileFilter = new FileFilter()
	{
		@Override
		public boolean accept(File file)
		{
			return file.isFile();
		}
	};
	private static FileFilter dirFilter = new FileFilter()
	{
		@Override
		public boolean accept(File file)
		{
			return file.isDirectory();
		}
	};


	public static Set<File> getDirectoryContents(File root)
	{
		Set<File> output = new HashSet<File>();
		Set<File> dirs = new HashSet<File>();

		output.addAll(Arrays.asList(root.listFiles(fileFilter)));
		dirs.addAll(Arrays.asList(root.listFiles(dirFilter)));

		for (File f : dirs)
		{
			if (f.isDirectory())
			{
				output.addAll(getDirectoryContents(f));
			}
		}

		return output;
	}

	public static boolean createZipArchive(File file, Set<File> files)
	{
		if (file.exists())
		{
			LogHelper.warn("Cannot create archive " + file + ": file already exists");
			return false;
		}

		try
		{
			FileOutputStream output = new FileOutputStream(file);
			ZipOutputStream zip = new ZipOutputStream(output);

			for (File f : files)
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
		}
		catch (IOException ex)
		{
			LogHelper.error("Error creating archive: " + ex.getMessage());
			ex.printStackTrace();
		}

		return false;
	}
}
