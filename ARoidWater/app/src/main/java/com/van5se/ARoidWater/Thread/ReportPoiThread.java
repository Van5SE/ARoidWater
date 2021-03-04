/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.van5se.ARoidWater.Thread;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.van5se.ARoidWater.Activity.MainActivity;
import com.van5se.ARoidWater.Activity.ReportActivity;
import com.van5se.ARoidWater.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ReportPoiThread implements Runnable {

    Context fatherActivity;
    String wiString;
    int workstatus;
    String TAG="ReportPoiThread";
    Socket socket;

    public ReportPoiThread(String wi, Context FatherActivity)
    {
        wiString=wi;
        fatherActivity=FatherActivity;
    }

    @Override
    public void run() {
        try
        {// 进行连接
            socket=new Socket();
            socket.connect(new InetSocketAddress(MainActivity.server, MainActivity.port), MainActivity.timeoutsecond);

            OutputStream os=socket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(os);
            dos.writeInt(200);
            dos.writeUTF(wiString);

            InputStream is=socket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            workstatus=dis.readInt();
            Log.i(TAG, "workstatus"+workstatus);
            Looper.prepare();
            if(workstatus==210)
            {
                new AlertDialog.Builder(fatherActivity)
                        .setTitle(R.string.report_success)
                        .setMessage(R.string.report_msg_success)
                        .setPositiveButton(R.string.ok,null)
                        .show();
            }
            else if(workstatus==220)
            {
                new AlertDialog.Builder(fatherActivity)
                        .setTitle(R.string.report_failed)
                        .setMessage(R.string.report_msg_failed_same)
                        .setPositiveButton(R.string.ok,null)
                        .show();
            }
            else {
                new AlertDialog.Builder(fatherActivity)
                        .setTitle(R.string.report_failed)
                        .setMessage(R.string.report_msg_failed)
                        .setPositiveButton(R.string.ok,null)
                        .show();
            }
            Looper.loop();
        }catch (SocketTimeoutException ste) {
            ste.printStackTrace();
            //通过Looper.prepare和loop防止冲突
            Looper.prepare();
            new AlertDialog.Builder(fatherActivity)
                    .setTitle(R.string.report_failed)
                    .setMessage(R.string.socket_timeout)
                    .setPositiveButton(R.string.ok,null)
                    .show();
            Looper.loop();
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(socket!=null)
            {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
