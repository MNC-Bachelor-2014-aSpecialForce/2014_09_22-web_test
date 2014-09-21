package com.example.mapt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

 Handler handler = new Handler()
 {
  public void handleMessage(android.os.Message msg)
  {
   Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();;
  }
 };
 
 class MyWebSocket extends WebSocketServer
 {

  public MyWebSocket(InetSocketAddress address) {
   super(address);
   // TODO Auto-generated constructor stub
  }

  @Override
  public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
   // TODO Auto-generated method stub
   Log.d("websocket", "onClose");
  }

  @Override
  public void onError(WebSocket arg0, Exception arg1) {
   // TODO Auto-generated method stub
   Log.d("websocket", arg1.getMessage());
  }

  @Override
  public void onMessage(WebSocket arg0, String msg) {
   // TODO Auto-generated method stub
   Log.d("websocket", msg);
   
   // 내부적으로 스레드로 동작하기 때문에 핸들러를 통해 액티비티의 UI를 갱신해야만 한다
   handler.sendMessage(handler.obtainMessage(0, msg));
  }

  @Override
  public void onOpen(WebSocket arg0, ClientHandshake arg1) {
   // TODO Auto-generated method stub
   Log.d("websocket", "onOpen");
   
   // 내부적으로 스레드로 동작하기 때문에 핸들러를 통해 액티비티의 UI를 갱신해야만 한다
   handler.sendMessage(handler.obtainMessage(0, "onOpen"));
  }
  
 }
 
 MyWebSocket wsServer;
 Button btnSend;
 EditText editMsg;
 
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);
  
  wsServer = new MyWebSocket(new InetSocketAddress(9005));
  wsServer.start();
  
  btnSend = (Button)findViewById(R.id.btnSend);
  editMsg = (EditText)findViewById(R.id.editMsg);
  
  btnSend.setOnClickListener(new OnClickListener() {
   
   @Override
   public void onClick(View v) {
    // TODO Auto-generated method stub
    
    // connections는 서버에 여러 클라이언트가 연결되 있을 경우 연결된 모두를 얻기 위해 사용
    // 만약 유니캐스트 사용하고 싶으면 위의 MyWebSocket의 onOpen의 패러미터를 리스트 형식으로 저장하면 된다
    Set<WebSocket> conns = wsServer.connections();
    
    for( WebSocket ws : conns )
    {
     String msg = editMsg.getText().toString();
     ws.send(msg);
    }
   }
  });
  
  
 }
 
 @Override
 protected void onDestroy() {
  // TODO Auto-generated method stub
  super.onDestroy();
  
  try {
   wsServer.stop();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (InterruptedException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 } 
} 