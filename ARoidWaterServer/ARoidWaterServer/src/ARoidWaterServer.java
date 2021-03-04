import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.net.InetAddress;

public class ARoidWaterServer 
{
    
    static final int port=8888;
	static ServerSocket serverSocket=null;
	
    public static final String filename="waterInfo.txt";
    
    public static void initARoidWaterServer () throws IOException
    {
        System.out.println(InetAddress.getLocalHost());
		try{
			serverSocket=new ServerSocket(port);
			int count=0;
			System.out.println("服务器已启动");
			File filepn=new File(filename);
			if(!filepn.exists())
			{
				filepn.createNewFile();
			}

			while (true){
				Socket socket=serverSocket.accept();
				++count;
				InputStream is=socket.getInputStream();
				DataInputStream dis=new DataInputStream(is);
				int workstatus=dis.readInt();
				System.out.println("工作状态码"+workstatus);
				System.out.println("客户端"+socket.getInetAddress().getHostAddress()+"已上线");
				System.out.println("目前同时有"+count+"个客户端上线");
				switch(workstatus){
					case 100://100表示需要获取最新的附近水坑数据
						Thread getpoiThread=new Thread(new GetPoiThread(socket));
						getpoiThread.start();
						count--;
					break;
					case 200:
						Thread setpoiThread=new Thread(new SetPoiThread(socket));
						setpoiThread.start();
						count--;
					break;
                }
			}

		}catch(IOException e){
			e.printStackTrace();
		}
    }

    
    public static void main(String[] args) throws Exception {
        initARoidWaterServer();
    }

}
