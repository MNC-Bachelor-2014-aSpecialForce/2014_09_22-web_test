<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="serverThread" class="hun.serverThread"></jsp:useBean>

<!DOCTYPE html>
<html>
<head>
 <script
  src="http://maps.googleapis.com/maps/api/js?key=AIzaSyDY0kkJiTPVd2U7aTOAwhc9ySH6oHxOIYM&sensor=false">
 </script>
 
 <script>
 var w;
 var g;
 
 var map;
 var myCenter=new google.maps.LatLng(35.115,128.975); 
 var marker;
 var com=0;
 
 google.maps.event.addDomListener(window, 'load', myStart);
 function WebSocketTest()
 {
   if ("WebSocket" in window)
   {
      alert("연결 되었습니다");
     
      var ws = new WebSocket("ws://192.168.0.9:9005/echo");
      ws.onopen = function()
      {
        
         ws.send("Message to send");
         alert("Message is sent...");
         
      };
      ws.onmessage = function (evt) 
      { 
         var received_msg = evt.data;
         alert(received_msg);
         
         map.panTo(new google.maps.LatLng(35.100, 128.965));
         marker.setPosition(new google.maps.LatLng(35.100, 128.965));
         marker.setMap(map);
        /* 
         var newPos=new google.maps.LatLng(35.105,128.975);
         marker.setPosition = newPos;
         marker.setMap(map);
         */
       	 
      };
      ws.onclose = function()
      { 
         alert("연결이 종료 되었습니다"); 
      };
   }
   else
   { 
      alert("지원하지 않는 브라우저 입니다");
   }
 }
 
 
 // var map;
 // var myCenter=new google.maps.LatLng(35.115,128.975); 
 // var marker;
  //var com=0;
 
  function myStart(){
   var mapProp = {
    center:myCenter, 
    zoom:80, 
    mapTypeId:google.maps.MapTypeId.HYBRID 
   };
 
   map=new google.maps.Map(document.getElementById("googleMap"),mapProp); 
 
   marker=new google.maps.Marker({
    position:myCenter,
    animation:google.maps.Animation.BOUNCE
   });
   marker.setMap(map);
  }
 
 
 </script>
</head>
 
<body>
 <div id="googleMap" style="width:600px;height:600px;"></div>
 <div id="sse">
   <a href="javascript:WebSocketTest()">Run WebSocket</a>
</div>
</body>
</html>
