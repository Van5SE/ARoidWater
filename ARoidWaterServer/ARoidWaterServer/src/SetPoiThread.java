
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.google.gson.Gson;

public class SetPoiThread implements Runnable {

    Socket socket = null;


    SetPoiThread(Socket s)
    {
        this.socket=s;
    }

    @Override
    public void run() {
       try {
            InputStream is=socket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            OutputStream os=socket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(os);
            String WarningInfo=dis.readUTF();
            Gson gson=new Gson();
            WarningInfo wi = gson.fromJson(WarningInfo, WarningInfo.class);
            System.out.println(wi.getLatitude());
            System.out.println(wi.getLongtitude());
            System.out.println(wi.getWarntype());
            System.out.println(wi.getTime());

                File file=new File(ARoidWaterServer.filename);
                if(!file.exists())
                {
                    file.createNewFile();
                }
                FileWriter fw=new FileWriter(file,true);
                FileReader fr=new FileReader(file);
                BufferedReader br=new BufferedReader(fr);
                String thisLine=null;
                Boolean writeflag=true;
                while((thisLine=br.readLine())!=null)
                {
                    WarningInfo thisInfo=gson.fromJson(thisLine, WarningInfo.class);
                    //如果已经保存过该点的同类型信息则不保存当前数据
                    if(thisInfo!=null)
                    {
                        if(thisInfo.getLatitude().equals(wi.getLatitude())&&thisInfo.getLongtitude().equals(wi.getLongtitude())
                                                                            &&thisInfo.getWarntype().equals(wi.getWarntype()))
                            {
                            writeflag=false;
                            break;
                            }
                    }
                }
                //writeflag为真时写入数据
                if(writeflag==true)
                {
                    fw.write(WarningInfo+'\n');
                    dos.writeInt(210);
                }
                else
                {
                    System.out.println("writeflag false");
                    dos.writeInt(220);
                }
                fw.close();
                br.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }



    
}
