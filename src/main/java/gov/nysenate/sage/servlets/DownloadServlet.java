package gov.nysenate.sage.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static String realPath;
	private static final String sep = System.getProperty("file.separator");
	
	 public void init(ServletConfig config) throws ServletException {
	    	super.init(config);
	    	
	    	realPath = getServletContext().getRealPath(sep).replaceAll(sep + "GeoApi.*$", "") 
				+ sep
	    		+ "complete" 
				+ sep;
	    }
       
    public DownloadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		String fileName = request.getRequestURI().replaceAll("/?(GeoApi/)?download/?","");
		
		File file = new File(realPath + fileName);
		if(!fileName.matches("\\s*") && file.exists()) {
			response.setHeader("Content-disposition", "attachement;filename=" + fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String in = null;
			while((in = br.readLine()) != null) {
				out.println(in);
			}
			br.close();
		}
		else {
			out.println("File not found.");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
