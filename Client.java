import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import Service.*;
import java.util.List;
import java.util.Scanner;
import java.util.Date;
import java.io.File;
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

public class Client {
	public static void main(String [] args) {
		try {
			TTransport transport;
			transport = new TSocket("localhost", 9090);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			Service.Client client = new
			Service.Client(protocol);
			perform(client);
			transport.close();
		}

		catch (TException x) {
			x.printStackTrace();
		}
	}
	
	private static void perform(Service.Client client) throws TException {
		Scanner sc = new Scanner(System.in);
		String temp;
		System.out.println("Input a command:");
		while (true) {
			if ((temp = sc.nextLine()) != null) {
				String[] command = temp.split("\\s+");

				if (command[0].equals("DIR")) {
					if (command.length > 1 && command[1] != null)
						dir(command[1], client);
					else
						dir("", client);
				}
				else if (command[0].equals("CREATEDIR")) {
					createDir(command[1], command[2], client);
				}
				else if (command[0].equals("GETFILE")) {
					getFile(command[1], command[2], command[3], client);
				}
				else if (command[0].equals("PUTFILE")) {
					putFile(command[1], command[2], command[3], client);
				}
				else if (command[0].equals("EXIT")) {
					break;
				}

				System.out.println();
			}
		}
	}

	private static void dir(String path, Service.Client client) throws TException {
		List<Data> fileList = client.dir(path);
		System.out.format("%22s %11s %28s %28s\n", "Name", "Size", "Last Modified", "Created At");
		for (Data data : fileList) {
			System.out.format("%22s %8d KB ", data.name, data.size/1024);
			System.out.println(new Date(data.lastModifiedDate) + "\t" + new Date(data.createdDate));
		}
	}

	private static void createDir(String path, String name, Service.Client client) throws TException {
		String result = client.createDir(path, name);
		System.out.println(result);
	}

	private static void getFile(String path, String name, String localPath, Service.Client client) throws TException {
		Data result = client.getFile(path, name);
		if (result != null) {
			try {
				File f = new File(localPath + name);
				FileChannel channel = new FileOutputStream(f, false).getChannel();
				channel.write(result.buffer);
				channel.close();

				Path p = Paths.get(f.getAbsolutePath());
				Files.setAttribute(p, "basic:lastModifiedTime", FileTime.fromMillis(result.lastModifiedDate), LinkOption.NOFOLLOW_LINKS);
				Files.setAttribute(p, "basic:creationTime", FileTime.fromMillis(result.createdDate), LinkOption.NOFOLLOW_LINKS);

				System.out.println("Getfile success");
			}
			catch (IOException e) {
				System.out.println(e);
			}
		}
		else {
			System.out.println("Server error");
		}
	}

	private static void putFile(String path, String name, String localPath, Service.Client client) throws TException {
		try {
			Data data = new Data();
			File f = new File(localPath + name);
			FileChannel channel = FileChannel.open(f.toPath());
			data.buffer = ByteBuffer.allocate((int) channel.size());
			data.size = channel.size();
			channel.read(data.buffer);
			data.buffer.flip();
			channel.close();

			Path p = Paths.get(f.getAbsolutePath());
			BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
			data.lastModifiedDate = view.lastModifiedTime().toMillis();
			data.createdDate = view.creationTime().toMillis();

	    String result = client.putFile(path, name, data);

	    System.out.println(result);
		}
		catch (IOException e) {
			System.out.println("Error");
		}
	}
}