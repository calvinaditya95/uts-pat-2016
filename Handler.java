import org.apache.thrift.TException;
import Service.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.LinkOption;

public class Handler implements Service.Iface {
	@Override
	public List<Data> dir(String path) throws TException {
		File dir;
		if (!path.equals(""))
			dir = new File(path);
		else
			dir = new File("/");

		List<Data> fileList = new ArrayList<Data>();

		for (File f : dir.listFiles()) {
			try {
				Data data = new Data();
				Path p = Paths.get(f.getAbsolutePath());
				BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();

				data.name = f.getName();
				if (f.isFile())
					data.size = view.size();
				else
					data.size = 0;

				data.lastModifiedDate = view.lastModifiedTime().toMillis();
				data.createdDate = view.creationTime().toMillis();
				fileList.add(data);
			}
			catch (IOException e) {
				System.out.println(e);
			}
		}

		return fileList;
	}

	@Override
	public String createDir(String path, String name) throws TException {
		File f = new File(path);
		if (!f.exists() || !path.substring(path.length()-1).equals("/")) {
			return "Path doesn't exist";
		}
		else {
			File newdir = new File(path + name);
			newdir.mkdir();
			return "Directory created";
		}
	}

	@Override
	public Data getFile(String path, String name) throws TException {
		File f = new File(path + name);
		if (!f.exists()) {
			System.out.println("File doesn't exist");
			return null;
		}
		else {
			try {
				Data data = new Data();

				Path p = Paths.get(f.getAbsolutePath());
				BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
				data.lastModifiedDate = view.lastModifiedTime().toMillis();
				data.createdDate = view.creationTime().toMillis();

				FileChannel channel = FileChannel.open(f.toPath());
				data.buffer = ByteBuffer.allocate((int) channel.size());
				channel.read(data.buffer);
				data.buffer.flip();
				channel.close();

				return data;
			}
			catch (IOException e) {
				System.out.println("IOException");
				return null;
			}
		}
	}

	@Override
	public String putFile(String path, String name, Data data) throws TException {
		try {
			File f = new File(path + name);
			FileChannel channel = new FileOutputStream(f, false).getChannel();
			channel.write(data.buffer);
			channel.close();

			Path p = Paths.get(f.getAbsolutePath());
			Files.setAttribute(p, "basic:lastModifiedTime", FileTime.fromMillis(data.lastModifiedDate), LinkOption.NOFOLLOW_LINKS);
			Files.setAttribute(p, "basic:creationTime", FileTime.fromMillis(data.createdDate), LinkOption.NOFOLLOW_LINKS);

			return "Putfile success";
		}
		catch (IOException e) {
			return "Server error";
		}
	}
}