package xf.ad.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IAction {
	public String Excute(HttpServletRequest request, HttpServletResponse response);
}
