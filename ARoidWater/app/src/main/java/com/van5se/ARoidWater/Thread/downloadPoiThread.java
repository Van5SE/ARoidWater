/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.van5se.ARoidWater.Thread;

import android.util.Log;

import com.google.gson.Gson;
import com.van5se.ARoidWater.Activity.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;


public class downloadPoiThread implements Runnable {

    String dLIString;
    String neededPoiJson;
    String TAG="debug_downloadPoiThread";
    Socket socket;

    public static ArrayList <String> neededPOI=new ArrayList<String>();

    public downloadPoiThread(String dLI)
    {
        dLIString=dLI;
    }

    @Override
    public void run() {
        try
        {// 进行连接
            socket=new Socket();
            socket.connect(new InetSocketAddress(MainActivity.server, MainActivity.port), MainActivity.timeoutsecond);

            OutputStream os=socket.getOutputStream();
            //100表示下载 1xx表示下载返回的状态码
            DataOutputStream dos=new DataOutputStream(os);
            dos.writeInt(100);
            dos.writeUTF(dLIString);

            InputStream is=socket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            neededPoiJson=dis.readUTF();
            Gson gson=new Gson();
            neededPOI=gson.fromJson(neededPoiJson,ArrayList.class);
            Log.i(TAG, "run: "+neededPOI.size());
            /**
             * 6个信息为needPOI
             * 分别为
             * i latitude
             * i+1 longtitude
             * i+2 warntype
             * i+3 time
             * i+4 deep
             * i+5 area
             */
            for(int i=0;i<neededPOI.size();i++)
            {
                Log.i(TAG,neededPOI.get(i));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(socket!=null)
                {
                    socket.close();
                }
            }catch (IOException ioe)
            {
                ioe.printStackTrace();
            }

        }
    }
}
