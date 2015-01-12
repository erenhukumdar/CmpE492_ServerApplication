package edu.boun.gglass.server;


import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import edu.boun.gglass.ui.uiCallbacks;

public class httpServer implements Runnable{
	private uiCallbacks callbackClass;
	private Server server;
	
	public httpServer(uiCallbacks arg){
		callbackClass=arg;
	}

	public static class FilePostHandler extends HandlerWrapper
	{
		private uiCallbacks callbackClass;
		private SecureRandom random = new SecureRandom();
		private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
		
		public String nextSessionId() {
		    return new BigInteger(130, random).toString(32);
		  }
		
		public FilePostHandler(uiCallbacks arg){
			callbackClass=arg;
		}
		
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
		{
			if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
				baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
			}

			String fileName = nextSessionId() + ".jpg";
			
			//get image (file input)
			request.getPart("image").write(fileName);
			
			//get file name (text input)
			//String fileName = request.getParameter("name");
			
			PrintWriter out = response.getWriter();
			response.setContentType("text/plain");
			out.printf("%s posted a photo\n",request.getRemoteAddr());
			out.printf("%s is saved under %s\n",fileName,System.getProperty("java.io.tmpdir"));
			
			System.out.printf("%s posted a photo\n",request.getRemoteAddr());
			System.out.printf("%s is saved under %s\n",fileName,System.getProperty("java.io.tmpdir"));

			callbackClass.onImageReceived(System.getProperty("java.io.tmpdir")+"/"+fileName, request.getRemoteAddr());
			
			baseRequest.setHandled(true);
		}
	}
	
	public static class SensorDataHandler extends HandlerWrapper
	{
		private uiCallbacks callbackClass;
		private SecureRandom random = new SecureRandom();
		
		public String nextSessionId() {
		    return new BigInteger(130, random).toString(32);
		  }
		
		public SensorDataHandler(uiCallbacks arg){
			callbackClass=arg;
		}
		
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
		{
//			String lightSensorFile = "LightSensorProbe.cvs";
//			String gyroscopeSensorFile = "GyroscopeSensorProbe.cvs";
//			String accelerometerSensorFile = "AccelerometerSensorProbe.cvs";
//			String magneticFieldSensorFile = "MagneticFieldSensorProbe.cvs";
			
			PrintWriter out = response.getWriter();
			response.setContentType("text/plain");
			out.printf("%s posted sensor data\n",request.getRemoteAddr());
			out.printf("All sensor data are saved\n");
			
			System.out.printf("%s posted sensor data\n",request.getRemoteAddr());
			System.out.printf("LightSensorProbe is\n%s\n",request.getParameter("LightSensorProbe"));
			System.out.printf("GyroscopeSensorProbe is\n%s\n",request.getParameter("GyroscopeSensorProbe"));
			System.out.printf("AccelerometerSensorProbe is\n%s\n",request.getParameter("AccelerometerSensorProbe"));
			System.out.printf("MagneticFieldSensorProbe is\n%s\n",request.getParameter("MagneticFieldSensorProbe"));

			baseRequest.setHandled(true);
		}
	}

	public void run(){
		server = new Server(8080);

		// collection for handlers
		HandlerCollection handlers = new HandlerCollection();
		server.setHandler(handlers);

		// photo upload context
		ContextHandler photoContext = new ContextHandler("/uploadphoto");
		photoContext.setAllowNullPathInfo(true); /*use this to avoid redirection due to missing trailing slash*/
		photoContext.setHandler(new FilePostHandler(callbackClass));
		handlers.addHandler(photoContext);
		
		// sensor data upload context
		ContextHandler sensorContext = new ContextHandler("/uploadsensor");
		sensorContext.setAllowNullPathInfo(true); /*use this to avoid redirection due to missing trailing slash*/
		sensorContext.setHandler(new SensorDataHandler(callbackClass));
		handlers.addHandler(sensorContext);

		// default handler
		// handlers.addHandler(new DefaultHandler());

		try {
			server.start();
			callbackClass.onHttpServerStart();
			
			//code will be blocked on next line
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			callbackClass.onHttpServerStop();
		}
	}
	
	public void stop(){
		try {
			server.stop();
			callbackClass.onHttpServerStop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}