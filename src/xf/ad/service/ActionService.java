package xf.ad.service;

import java.io.IOException;
import java.io.PrintWriter;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import xf.ad.action.IAction;


@WebServlet("/ActionService")
public class ActionService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActionService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub	
		response.setContentType("text/html;charset=UTF-8");
        //response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		String actionType = request.getParameter("action");
		String resultStr = "";
		try {
			IAction action=ActionFactory.getAction(actionType);
			resultStr=action.Excute(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.print(resultStr);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=UTF-8");
        //response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		String actionType = request.getParameter("action");
		String resultStr = "";
		try {
			IAction action=ActionFactory.getAction(actionType);
			resultStr=action.Excute(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.print(resultStr);
	}
}
