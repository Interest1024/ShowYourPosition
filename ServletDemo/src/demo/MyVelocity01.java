package demo;

import java.io.*;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class MyVelocity01 extends HttpServlet {
	
	//The path of log file
	private static String sFileName = ".\\UserLatLon.log";
	
	/*
	 * Deal with GET request
	 * Make web page using VelocityEngine and template
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		Properties properties = new Properties();
		properties.setProperty("resource.loader", "class");
		properties.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// properties.setProperty("input.encoding", "UTF-8");
		// properties.setProperty("output.encoding", "UTF-8");
		properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
		properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
		
		VelocityEngine velocityEngine = new VelocityEngine(properties);
		VelocityContext context = new VelocityContext();
		context.put("name", "test");
				
		//save the requestUri in the request
		//the requestUri will be used in template and be used to compose POST request
		String requestUri = request.getRequestURI();
		context.put("requesturi", requestUri);
		
		
		StringWriter sw = new StringWriter();
		
		velocityEngine.mergeTemplate("templates/test.vm", "utf-8", context, sw);
		
		out.println(sw.toString());
		
	}
	
	/*
	 * Deal with POST request
	 * Save user location information into log file
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//System.out.println("MyVelocity01::doPost:entrance");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		ServletInputStream in = request.getInputStream();  
		String strLatLon = readLine(in);
		//System.out.println("MyVelocity01::doPost:input string: "+strValue);

		if(strLatLon == null){
			returnResponse(response, "Latitude and longitude are null.");
			return;
		}
		
		String[] strArrayLatLon = strLatLon.split(",");
		if(strArrayLatLon.length<2 || strArrayLatLon[0].trim() == "" || strArrayLatLon[1].trim()==""){
			returnResponse(response, "Latitude or longitude are null.");
			return;
		}

		double dLat = Double.parseDouble(strArrayLatLon[0]);
		double dLon = Double.parseDouble(strArrayLatLon[1]);
		System.out.println("MyVelocity01::doPost:"+dLat+","+dLon);
		
		Date dateNow = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String strNow = dateFormat.format( dateNow ); 
		//System.out.println(strNow)
		writeToFile(sFileName,dLat,dLon,strNow);
		
		returnResponse(response, "Latitude and longitude are saved.");
		return;
		
	}
	
	/*
	 * Return information to POST request
	 */
	private void returnResponse(HttpServletResponse response, String pStrResponse) throws IOException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(pStrResponse);
		out.flush();
		out.close();
	}
	
	/*
	 * Get String from the POST request.
	 */
	private String readLine(ServletInputStream in) throws IOException{  
        byte[] buf = new byte[8 * 1024];  
        StringBuffer sbuf = new StringBuffer();  
        int result;  
        // String line;  
  
        do {  
            result = in.readLine(buf, 0, buf.length); // does +=  
            if(result != -1) {  
                sbuf.append(new String(buf, 0, result, "UTF-8"));  
            }  
        }  
        while(result == buf.length); // loop only if the buffer was filled  
  
        if(sbuf.length() == 0) {  
            return null; // nothing read, must be at the end of stream  
        }  
  
        // Cut off the trailing \n or \r\n  
        // It should always be \r\n but IE5 sometimes does just \n  
        /*
        int len = sbuf.length();  
        if(sbuf.charAt(len - 2) == '\r') {  
            sbuf.setLength(len - 2); // cut \r\n  
        }  
        else {  
            sbuf.setLength(len - 1); // cut \n  
        } 
        */ 
        return sbuf.toString();  
    }  
	
	/*
	 * Save timestamp, latitude, longitude to log file
	 */
	private void writeToFile(String pFileName, double dLat,double dLon,String strNow){
		 BufferedWriter writer = null;
	        File file = new File(pFileName);
	        //if file does not exist, then create one.
	        if(!file.exists()){
	            try {
	                file.createNewFile();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        //write
	        try {
	            writer = new BufferedWriter(new FileWriter(file,true));
	            String strLine = "{\"timestramp\":\""+strNow+"\",\"Lat\":"+Double.toString(dLat)+",\"Lon\":"+Double.toString(dLon)+"}\n";
		        System.out.println("MyVelocity01::writeToFile:add a line to log: "+strLine);
	            writer.write(strLine);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }finally {
	            try {
	                if(writer != null){
	                    writer.close();
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
//	        System.out.println("write successfully");
	}
}

