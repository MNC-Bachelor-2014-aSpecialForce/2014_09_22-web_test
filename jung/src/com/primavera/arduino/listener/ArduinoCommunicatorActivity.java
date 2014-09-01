package com.primavera.arduino.listener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ArduinoCommunicatorActivity extends Activity {
	
	public int IAMNOTHING = 1;
	
	ServerSocket serverSocket;

	Handler updateConversationHandler;

	ServerThread serverThread;

	private TextView text;

	public static final int SERVERPORT = 8039;
	
	public boolean gg=false;
	
	
	

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;  //9025
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01; //1
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10; //16
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42; //66
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43; //67
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44; //68
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F; //63

    private final static String TAG = "ArduinoCommunicatorActivity";
    private final static boolean DEBUG = false;
    public Socket socket = null;
    
    private Boolean mIsReceiving;
    private ArrayList<ByteArray> mTransferedDataList = new ArrayList<ByteArray>();
    private ArrayAdapter<ByteArray> mDataAdapter;

    private void findDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        if (DEBUG) Log.d(TAG, "length: " + usbDeviceList.size());
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();

         
            if (DEBUG) Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
            if (DEBUG) Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
            if (DEBUG) Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            if (DEBUG) Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            if (DEBUG) Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            if (DEBUG) Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            if (DEBUG) Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            if (DEBUG) Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

        
                if (DEBUG) Log.i(TAG, "Arduino device found!");

                switch (tempUsbDevice.getProductId()) {
                case ARDUINO_UNO_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Uno " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Uno R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                }
            
        }

        if (usbDevice == null) {
            if (DEBUG) Log.i(TAG, "No device found!");
            Toast.makeText(getBaseContext(), getString(R.string.no_device_found), Toast.LENGTH_LONG).show();
        } else {
            if (DEBUG) Log.i(TAG, "Device found!");
            Intent startIntent = new Intent(getApplicationContext(), ArduinoCommunicatorService.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, startIntent, 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        if (DEBUG) Log.d(TAG, "onCreate()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
        filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
        registerReceiver(mReceiver, filter); // onReceive�� ���� ȣ��� ������ �ٲ�

        mDataAdapter = new ArrayAdapter<ByteArray>(this, android.R.layout.simple_list_item_1, mTransferedDataList);
        ListView list = (ListView)findViewById(R.id.list);
        list.setAdapter(mDataAdapter);
        
        //setListAdapter(mDataAdapter);
        text = (TextView) findViewById(R.id.text2);

		updateConversationHandler = new Handler();

		this.serverThread = new ServerThread();
		this.serverThread.start();
        findDevice();
        
        
    }

    
    public class ServerThread extends Thread implements Runnable {
    	private PrintWriter pw;
    	public Handler mBackHandler;
    	
    	int a;


 

		public void run() {
			
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//while (!Thread.currentThread().isInterrupted()) {
			
			
			

			try {
				socket = serverSocket.accept();
				gg=true;

				CommunicationThread commThread = new CommunicationThread(socket);
				new Thread(commThread).start();
		
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		
			if(gg){
			Looper.prepare();
			mBackHandler = new Handler(){
				public void handleMessage(Message msg){
					if( msg.what == 0 ){
						try {
							socket.getOutputStream().write((byte [])msg.obj);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
			Looper.loop();
			}
				
			
				
			//}
		}
	}
	//msg.obj
	/*
	Looper.prepare();
	
	 mBackHandler = new Handler(){
		 public void handleMessage(Message msg) {
               Message retmsg = new Message();
               try {
					pw.write(new String("Write some"));
					

				} catch (Exception e) {
					e.printStackTrace();
				}
		 }
	Looper.loop();
		 */
	/*Looper.prepare();
	 mBackHandler = new Handler(){
		 public void handleMessage(Message msg) {
               Message retmsg = new Message();
               if(retmsg.what==0) {
            	   String a=Integer.toString(retmsg.arg1);
               try {
					pw.write(new String(a));
					

				} catch (Exception e) {
					e.printStackTrace();
				}
               }
		 }
	 };
		 
		 
		 Looper.loop();
	// };

	*/
	
	
	
	//OutputThread outThread = new OutputThread(socket);
	
	//new Thread(outThread).start();
    
    
    
    
    
    
    class CommunicationThread implements Runnable {

		private Socket clientSocket;
		Scanner inp;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;
			

			try {
				this.inp=new Scanner(this.clientSocket.getInputStream());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		

		public void run() {

			while (!Thread.currentThread().isInterrupted()) {
				//String read = inp.nextLine();
//------------------------------------------------------------------
				byte [] read = new byte[8];
				
				try {
					socket.getInputStream().read(read, 0, 8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//------------------------------------------------------------------
				
				updateConversationHandler.post(new updateUIThread(read));
				//new updateUIThread(read);
			}
		}

	}
    

    
//    class OutputThread extends Thread implements Runnable {
//
//		private Socket clientSocket;
//
//		//private BufferedReader input;
//		private PrintWriter pw;//
//		String a;
//		
//		
//		public OutputThread(Socket clientSocket,String a) {
//
//			this.clientSocket = clientSocket;
//			this.a=a;
//
//			try {
//				pw = new PrintWriter(this.clientSocket.getOutputStream());
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		public void run() {
//			while (!Thread.currentThread().isInterrupted()) {
//
//				try {
//					this.pw.write(new String(a));
//					
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}
//
//	}
    
    
    
    
    
    
    class updateUIThread implements Runnable {
		private byte[] msg;

		public updateUIThread(byte [] read) {
			this.msg = read;
		}

		@Override
		public void run() {
			text.setText(text.getText().toString()+ msg);

		
			if( msg.length > 0 ){
				//Toast.makeText(getBaseContext(), "transmiter", Toast.LENGTH_SHORT).show();
				Intent intent_going = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
	            
	            intent_going.putExtra(ArduinoCommunicatorService.DATA_EXTRA, msg);
	            sendBroadcast(intent_going);
			}
		}
	}

    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        if (DEBUG) Log.i(TAG, "onListItemClick() " + position + " " + id);
//        ByteArray transferedData = mTransferedDataList.get(position);
//        transferedData.toggleCoding();
//        mTransferedDataList.set(position, transferedData); // ����Ʈ�� position�� ��ġ�� ���� transferedData(bytearray) �� ��ü
//        mDataAdapter.notifyDataSetChanged();
//    }
    
    protected void onStop() {
		super.onStop();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
            findDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.help:
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://ron.bems.se/arducom/usage.html")));
            return true;
        case R.id.about:
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://ron.bems.se/arducom/primaindex.php")));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
  /*  Handler mHandler = new Handler() {
		public void handleMessage(String msg) {
			
				//mBackText.setText("BackValue : " + mBackValue);
			
		}
	};
    
    */
	
    void handleTransferedData(Intent intent, boolean receiving) {
    	
    	Message msg;
        if (mIsReceiving == null || mIsReceiving != receiving) {
            mIsReceiving = receiving;
            mTransferedDataList.add(new ByteArray());
        }

        final byte[] newTransferedData = intent.getByteArrayExtra(ArduinoCommunicatorService.DATA_EXTRA);
        if (DEBUG) Log.i(TAG, "data: " + newTransferedData.length + " \"" + new String(newTransferedData) + "\"");

        ByteArray transferedData = mTransferedDataList.get(mTransferedDataList.size() - 1);
        transferedData.add(newTransferedData);
        transferedData.mShowInAscii=true;
        mTransferedDataList.set(mTransferedDataList.size() - 1, transferedData);
        
        mDataAdapter.notifyDataSetChanged();
        
        
        
        
        msg = new Message(); //�޽����� ����� �޽����� ���� �ֱ�
        msg.what = 0;
        msg.obj = newTransferedData;
        if (gg){
        serverThread.mBackHandler.sendMessage(msg);
        }
    }
 

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive() " + action);

            if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) {
                handleTransferedData(intent, true);
            } else if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action)) {
                handleTransferedData(intent, false);
            }
        }
    };
}
