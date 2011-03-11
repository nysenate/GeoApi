package gov.nysenate.sage.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UploadMessageServlet
 */
public class UploadMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadMessageServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("email") ==  null 
				&& request.getSession().getAttribute("queue") == null 
				&& request.getSession().getAttribute("error") == null) {
			
			response.sendRedirect("/GeoApi/upload");
		}
		else {
			request.setAttribute("email", request.getSession().getAttribute("email"));
			request.setAttribute("queue", request.getSession().getAttribute("queue"));
			request.setAttribute("records", request.getSession().getAttribute("records"));
			request.setAttribute("error", request.getSession().getAttribute("error"));
			request.getSession().setAttribute("email", null);
			request.getSession().setAttribute("queue", null);
			request.getSession().setAttribute("records", null);
			request.getSession().setAttribute("error", null);
			request.getRequestDispatcher("/message.jsp").forward(request, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
