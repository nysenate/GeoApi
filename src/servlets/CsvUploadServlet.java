package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BulkProcessing.JobProcess;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import BulkProcessing.Mailer;
import BulkProcessing.Processor;

import control.Connect;

public class CsvUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public CsvUploadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("upload.jsp").forward(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(52428800);
		factory.setRepository(new File("WebContent/upload/"));

		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setSizeMax(52428800);
		
		String fileName = null;
		String contact = null;
		String jobType = null;
		
		FileItem file = null;

		try {
			List<FileItem> items = upload.parseRequest(request);
			
			for(FileItem fi:items) {
				if(fi.isFormField()) {
					if(fi.getFieldName().equals("email")) {
						contact = fi.getString();
					}
					else if(fi.getFieldName().equals("format")) {
						jobType = fi.getString();
					}
				}
				else 
				{
					file = fi;
				}
			}
			
			fileName = contact.split("\\@")[0] 
                    + "-"
			        + (new Date().getTime()) 
			        + "-"
			        + file.getName().split("\\.")[0] 
			       
					+ ".csv";
			String sep = System.getProperty("file.separator");
			try {
				String path = getServletContext().getRealPath(sep).replace("\\GeoApi", "") 
						+ "upload" 
						+ sep 
						+ fileName;
								
				assignJobProcessInformation(request);
												
				Connect connect = new Connect();
				connect.persist(new JobProcess(
						contact,
						jobType,
						fileName,
						writeFile(path,new InputStreamReader(file.getInputStream()))));
				
				connect.close();
				
				
				request.getSession().setAttribute("email", contact);
				
			} catch (Exception e) {
				request.getSession().setAttribute("error", "Sorry, there was an error processing your request.  " +
						"An email has been dispatched and you will be contacted shortly.");
				try {
					Mailer.sendMail("williams@nysenate.gov",
							"bulk upload error",
							contact + " - " + jobType + " - " + fileName + "<br/><br/>" + e.getMessage(),
							"williams@nysenate.gov",
							"SAGE Bulk front-end error");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.getMessage();
				
			}
		} catch (FileUploadException e) {
			request.setAttribute("error", "Sorry, there was an error processing your request.  " +
			"An email has been dispatched and you will be contacted shortly.");
			e.printStackTrace();
		}
		
		response.sendRedirect("/GeoApi/upload/message");
	}
	
	public void assignJobProcessInformation(HttpServletRequest request) {
		TreeSet<JobProcess> set = JobProcess.getJobProcesses();
		request.getSession().setAttribute("queue", set.size());
		
		int records = 0;
		for(JobProcess jp:set) {
			records += jp.getLineCount();
		}
		
		request.getSession().setAttribute("records", records);
	}

	public int writeFile(String path, InputStreamReader is) {
		if(is == null)
			return 0;
		
		try {
			int count = 0;
			String in = null;
			BufferedReader br = new BufferedReader(is);
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
			while((in = br.readLine()) != null) {
				bw.write(in + "\n");
				count++;
			}
			br.close();
			bw.close();
			
			return count;
		}
		catch (Exception e) {
			return 0;
		}
		
	}
}
