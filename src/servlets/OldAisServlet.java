package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.OldAisCount;

import control.Connect;

/**
 * 
 * @author Jared Williams
 *
 */
public class OldAisServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public OldAisServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String host = request.getRemoteAddr();
		
		OldAisCount oac = new OldAisCount(host);
		
		
		Connect c = new Connect();
		
		c.persist(oac);
		
		c.close();
		
		response.sendRedirect("http://senatehomepage.senate.state.ny.us/~AIS2/district.html");
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
