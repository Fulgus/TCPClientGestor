package com.santosjosemaria.tcpclientgestor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;



public class Connection extends AppCompatActivity {
    public static final String MSG_BOX = "com.santosjosemaria.tcpclientgestor.MESSAGE";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    Button send, close, sendArd;
    EditText serverIp, port;
    TextView msgBox;
    ImageView antena;
    String fileToSend="/storage/emulated/0/Download/message.txt";
    String msg;
    String msgArd;

    String answer;
    private Socket soc;
    private String endmsg = "ENDMSG";

    protected String start(String msg) throws IOException {
        String input;
        Integer leng;
        //while (true) {
        //input = scanner.nextLine();

        input = msg;
        leng = input.getBytes().length;
        PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
        out.println(leng);
        Log.d("LONGITUD", leng.toString());
        out.flush();
        out.println(input);
        Log.d("OUTPUT", input);
        out.flush();


        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));

        String str = in.readLine();

        return str;
        //}
    }



    protected String startArd(String msg) throws IOException {
        Integer leng1;
        int mid = msg.length()/2;
        String[] parts = {msg.substring(0,mid), msg.substring(mid)};

        leng1 = parts[0].getBytes().length;
        parts[0].replace("\n","");
        parts[1].replace("\n","");

        PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
        out.println(leng1);
        out.flush();
        out.println(parts[0]+parts[1]);
        out.flush();


        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));

        String str = in.readLine();

        return str;
    }

    protected void end() throws IOException {
        int endmsglen;
        endmsglen = endmsg.getBytes().length;
        PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
        out.println(endmsglen);
        out.flush();
        out.println(endmsg);
        out.flush();

        soc.close();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        int hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        serverIp = (EditText) findViewById(R.id.ipHolder);
        port = (EditText) findViewById(R.id.portHolder);
        msgBox = (TextView) findViewById(R.id.msgBox);
        antena = (ImageView) findViewById(R.id.antenna);

        send = (Button) findViewById(R.id.connect);
        sendArd = (Button) findViewById(R.id.sendArd);
        close = (Button) findViewById(R.id.closeConn);

        serverIp.setText("192.168.0.27");
        port.setText("2004");


        sendArd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Thread tInit = new Thread(new Runnable() {
                        public void run() {
                            try {
                                soc = new Socket(InetAddress.getByName(serverIp.getText().toString()), Integer.parseInt(port.getText().toString()));
                            }
                            catch (Exception e){

                            }


                        }
                    });


                    tInit.start();
                    tInit.join();


                    Thread tStartArd = new Thread(new Runnable() {
                        public void run(){
                            try{
                                answer = startArd(msgArd);
                            }
                            catch (Exception e){
                            }
                        }
                    });

                    tStartArd.start();
                    tStartArd.join();

                    Toast.makeText(getApplicationContext(), "Message Sent to the Arduino", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }



        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    File f = new File(fileToSend);
                    FileInputStream inputStream = new FileInputStream(f.getAbsolutePath());
                    msg = IOUtils.toString(inputStream, "UTF-8");
                    IOUtils.closeQuietly(inputStream);
                    Thread tInit = new Thread(new Runnable() {
                        public void run() {
                            try {
                                soc = new Socket(InetAddress.getByName(serverIp.getText().toString()), Integer.parseInt(port.getText().toString()));
                            }
                            catch (Exception e){

                            }
                        }
                    });

                    tInit.start();
                    tInit.join();

                    Thread tStart = new Thread(new Runnable() {
                        public void run(){
                            try{
                                answer = start(msg);
                            }
                            catch (Exception e){
                            }
                        }
                    });

                    tStart.start();
                    tStart.join();

                    Log.d("ANSWER", answer);
                    antena.setVisibility(View.INVISIBLE);
                    send.setVisibility(View.INVISIBLE);
                    msgBox.setText(answer);

                    msgArd = msgBox.getText().toString();



                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                try {
                    Thread tEnd = new Thread(new Runnable() {
                        public void run() {
                            try {
                                end();
                            }
                            catch (Exception e) {
                            }
                        }
                    });

                    tEnd.start();
                }
                catch(Exception e){

                }
            }
        });
    }
}
