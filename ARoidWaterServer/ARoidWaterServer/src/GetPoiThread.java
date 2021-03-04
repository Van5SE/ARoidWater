import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;

public class GetPoiThread implements Runnable {

    Socket socket=null;

    ArrayList <String> neededPOI=new ArrayList<String>();
    String neededPoi=null;

    GetPoiThread(Socket s)
    {
        this.socket=s;
    }

    @Override
    public void run() {
       try {
            InputStream is=socket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            String deviceLocInfo=dis.readUTF();
            Gson gson=new Gson();
            DeviceLocInfo dli=gson.fromJson(deviceLocInfo, DeviceLocInfo.class);
            System.out.println(dli.getLatitude());
            System.out.println(dli.getLongtitude());
            System.out.println(dli.getDistance());
            System.out.println(dli.getTime());

            checkPoi(Double.parseDouble(dli.getLatitude()), Double.parseDouble(dli.getLongtitude()),
                    Double.parseDouble(dli.getDistance()));

            
            OutputStream os=socket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(os);

            dos.writeUTF(neededPoi);

       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    //寻找符合条件的经纬度坐标点
    public void checkPoi(double la,double lo,double dis)
    {
        try {
            File file=new File(ARoidWaterServer.filename);
            Gson gson=new Gson();
            if(!file.exists())
            {
                file.createNewFile();
            }
            FileReader fReader=new FileReader(file);
            BufferedReader br=new BufferedReader(fReader);
            String thisLine=null;
            while((thisLine=br.readLine())!=null)
            {
                if(thisLine!=null)
                {
                    WarningInfo thisInfo=gson.fromJson(thisLine, WarningInfo.class);

                    double latb=Double.parseDouble(thisInfo.getLatitude());            
                    double lngb=Double.parseDouble(thisInfo.getLongtitude());
                   
                    double s=getDistance(la, lo, latb, lngb);
    
                    System.out.println(thisInfo.getWarntype()+" "+s+" m");
    
                    /**
                     * 符合寻找条件
                     */
                    if(s<dis)
                    {
                        neededPOI.add(thisInfo.getLatitude());
                        neededPOI.add(thisInfo.getLongtitude());
                        neededPOI.add(thisInfo.getWarntype());
                        neededPOI.add(thisInfo.getTime());
                        neededPOI.add(thisInfo.getDeep());
                        neededPOI.add(thisInfo.getArea());
                    }
                }
            }

            neededPoi=gson.toJson(neededPOI);
            br.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    public static double getDistance(Double lat1,Double lng1,Double lat2,Double lng2) {

        // 经纬度（角度）转弧度。弧度用作参数，以调用Math.cos和Math.sin
        double radiansAX = Math.toRadians(lng1); // A经弧度
        double radiansAY = Math.toRadians(lat1); // A纬弧度
        double radiansBX = Math.toRadians(lng2); // B经弧度
        double radiansBY = Math.toRadians(lat2); // B纬弧度


        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX)
            + Math.sin(radiansAY) * Math.sin(radiansBY);
    //        System.out.println("cos = " + cos); // 值域[-1,1]

        double acos = Math.acos(cos); // 反余弦值
    //        System.out.println("acos = " + acos); // 值域[0,π]
    //        System.out.println("∠AOB = " + Math.toDegrees(acos)); // 球心角 值域[0,180]
       return 6378137 * acos; // 最终结果

    }

    
}
