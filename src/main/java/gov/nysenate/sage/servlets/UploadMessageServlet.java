package gov.nysenate.sage.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UploadMessageServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("email") ==  null)
			response.sendRedirect("/GeoApi/upload");
		else {
			request.setAttribute("email", request.getSession().getAttribute("email"));
			request.getSession().removeAttribute("email");
			request.getRequestDispatcher("/message.jsp").forward(request, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
