package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class CsvUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public CsvUploadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(52428800);
		factory.setRepository(new File("WebContent/upload/"));

		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setSizeMax(52428800);
		
		String fName = null;
		String email = null;
		String format = null;
		
		FileItem file = null;

		try {
			List<FileItem> items = upload.parseRequest(request);
			
			for(FileItem fi:items) {
				if(fi.isFormField()) {
					if(fi.getFieldName().equals("email")) {
						email = fi.getString();
					}
					else if(fi.getFieldName().equals("format")) {
						format = fi.getString();
					}
				}
				else 
				{
					file = fi;
				}
			}
			
			fName = email.split("\\@")[0] 
			        + "-"
			        + file.getName().split("\\.")[0] 
			        + "-"
			        + (new Date().getTime()) 
					+ ".csv";
			String sep = System.getProperty("file.separator");
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(
									getServletContext().getRealPath(sep) 
									+ "upload" 
									+ sep 
									+ fName)));
			
			bw.write(email + ":" + format + "\n");
			
			String in = null;
			
			while((in = br.readLine()) != null) {
				bw.write(in + "\n");
			}
			
			br.close();
			bw.close();
			
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
