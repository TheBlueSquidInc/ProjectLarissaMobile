package com.thebluesquid.projectlarissa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.PrecomputedText;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {
    //Why cant I add  this code here with app crashing?
    // "Spinner sp1Transport = (Spinner) findViewById(R.id.spinner);"
    volatile String recText = null;


    public void runNetworkRequest(View view) throws IOException {
        //sanatizeInputs();

        Spinner sp1Transport = (Spinner) findViewById(R.id.spinner);
        Spinner sp2Sndrec = (Spinner) findViewById(R.id.spinner2);
        EditText destIpText = (EditText) findViewById(R.id.editTextDestIp);
        EditText destPortText = (EditText) findViewById(R.id.editTextPortNumber);
        EditText inputText = (EditText) findViewById(R.id.editTextTextMultiLine);

        String str1TransportVar = sp1Transport.getSelectedItem().toString();
        String str2SendRecVar = sp2Sndrec.getSelectedItem().toString();
        String str3DestIpVar = destIpText.getText().toString();
        String str4DestPortVar = destPortText.getText().toString();
        String str5InputTextVar = inputText.getText().toString();

/*        if(sanatizeDestIp(str3DestIpVar)){
            inputText.setText("IP is Valid! Congrats!");
        } else if (!sanatizeDestIp(str3DestIpVar)){
            inputText.setText("IP is burnt, bitch! HaHa!");
        }*/

        //

/*        if(sanatizeDestPort(Integer.parseInt(str4DestPortVar))){

            inputText.setText("Port number is Valid! Congrats!");

        } else if(!sanatizeDestPort(Integer.parseInt(str4DestPortVar))){
            inputText.setText("Port number is burnt, bitch! HaHa!");
        }*/

      String testStr = sp2Sndrec.getSelectedItem().toString();
      String testStr2 = "Send";
        String testStr3 = "Receive";

        if (sanatizeDestIp(str3DestIpVar) && sanatizeDestPort(Integer.parseInt(str4DestPortVar))) {

            //inputText.setText("Direction selected: " + sp2Sndrec.getSelectedItem().toString());
            Toast.makeText(getApplicationContext(), "Sending", Toast.LENGTH_SHORT);

            if(testStr.equals(testStr2)){
                byte[] data = inputText.getText().toString().getBytes();
                sendUdpPkt(str3DestIpVar, Integer.parseInt(str4DestPortVar), data);
                inputText.setText("Packet Sent! Congrats!");
            } else if(testStr.equals(testStr3)){
                receiveUdpPkt(Integer.parseInt(str4DestPortVar));
            }



        } else {
            inputText.setText("Info is burnt, bitch! HaHa!");

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toast.makeText(this, "Test!", Toast.LENGTH_SHORT);


        try {
            getLocalHostInfo();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void getLocalHostInfo() throws SocketException {

        Runnable rT = new Runnable() {
            @Override
            public void run() {
                TextView lHostName = (TextView) findViewById(R.id.textView8);
                TextView lHostAddr = (TextView) findViewById(R.id.textView9);
                TextView lHostMac = (TextView) findViewById(R.id.textView10);
                NetworkInterface nI = null;
                byte[] macBytes = null;
                try {
                    nI = NetworkInterface.getByName("wlan0");
                    macBytes = nI.getHardwareAddress();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                String[] str2 = {"test"};
                String macStr = " ";
                for (byte b : macBytes) {
                    macStr += String.format("%02x", b);
                }

                Enumeration<InetAddress> inetEnum = nI.getInetAddresses();
                InetAddress inetLocalHostData = null;
                int counter = 0;
                String name = null;
                // Regex for digit from 0 to 255.
                String zeroTo255
                        = "(\\d{1,2}|(0|1)\\"
                        + "d{2}|2[0-4]\\d|25[0-5])";
                // Regex for a digit from 0 to 255 and
                // followed by a dot, repeat 4 times.
                // this is the regex to validate an IP address.
                String regex
                        = zeroTo255 + "\\."
                        + zeroTo255 + "\\."
                        + zeroTo255 + "\\."
                        + zeroTo255;

                //Boolean isIp = Pattern.matches(regex, destIp);
                for (InetAddress i : Collections.list(inetEnum)) {
                    name = i.getHostAddress();
                    if (Pattern.matches(regex, name)) {
                        inetLocalHostData = i;
                    }
                }

                lHostMac.setText(macStr);
                lHostAddr.setText(inetLocalHostData.getHostAddress());
                lHostName.setText(inetLocalHostData.getHostName());
            }
        };
        new Thread(rT).start();

    }

    public Boolean sanatizeDestIp(String destIp) {

        // Regex for digit from 0 to 255.
        String zeroTo255
                = "(\\d{1,2}|(0|1)\\"
                + "d{2}|2[0-4]\\d|25[0-5])";
        // Regex for a digit from 0 to 255 and
        // followed by a dot, repeat 4 times.
        // this is the regex to validate an IP address.
        String regex
                = zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255;

        Boolean isIp = Pattern.matches(regex, destIp);
        return isIp;

    }

    public Boolean sanatizeDestPort(int portNum) {

        String portRegex = "^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[2-9]\\d{3}|1[1-9]\\d{2}|10[3-9]\\d|102[4-9])$";
        Boolean isValid = Pattern.matches(portRegex, Integer.toString(portNum));
        return isValid;

    }

    public void sendUdpPkt(String destIp, int destPort, byte[] byteArray) {

        final String destIp_1 = destIp;
        final int destPort_1 = destPort;
        final byte[] byteArray_1 = byteArray;


        Runnable udpThread = new Runnable() {
            @Override
            public void run() {

                try {
                    TextView lHostAddr = (TextView) findViewById(R.id.textView9);
                    DatagramSocket dgSkt =  new DatagramSocket();
                    final InetAddress inetObj = InetAddress.getByName(destIp_1);
                    DatagramPacket dgPkt = new DatagramPacket(byteArray_1, 0, byteArray_1.length, inetObj, destPort_1);
                    dgSkt.send(dgPkt);
                    dgSkt.close();
                    EditText inputText = (EditText) findViewById(R.id.editTextTextMultiLine);
                    inputText.setText("Sent to: " + inetObj.getHostAddress() + "@" + destPort_1);
                } catch ( IOException e) {
                    e.printStackTrace();
                }
            }
        }; new Thread(udpThread).start();

    }

    public void receiveUdpPkt(int portNum) {


        new Thread(new Runnable () {
            Handler mHandler = new Handler();
            @Override
            public void run() {

                String rcvdData = null;
                try {
                    DatagramSocket dgSkt  = new DatagramSocket(portNum);
                    byte[] recDataArray = new byte[65000];
                    DatagramPacket dgPkt = new DatagramPacket(recDataArray, recDataArray.length);
                    dgSkt.setSoTimeout(10000);
                    dgSkt.receive(dgPkt);
                      rcvdData = new String(dgPkt.getData(), 0, recDataArray.length);
                    dgSkt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String rcvdDataTrans = rcvdData;


                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText inputText = (EditText) findViewById(R.id.editTextTextMultiLine);
                        inputText.setText(rcvdDataTrans);
                    }
                });

            }
        }).start();
    }


}