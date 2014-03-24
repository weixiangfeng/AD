/**
 * 
 */
package xf.ad.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import xf.ad.model.CommonBean;
import xf.ad.service.Tools;

import com.google.gson.Gson;

public class ComboAction implements IAction {
	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {
		Connection connection = null;
		String resultString = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			ResultSetHandler<List<CommonBean>> handler = new BeanListHandler<CommonBean>(
					CommonBean.class);
			String type = request.getParameter("type");
			List<CommonBean> list = null;
			switch (type) {
			case "adtype":
				list = runner
						.query(connection,
								"select ad_code  as code,ad_name as name from basic_ad_type order by sort",
								handler);
				break;
			case "area":
				list = runner
						.query(connection,
								"select area_code as code,area_name as name from area order by sort",
								handler);
				break;
			case "subarea":
				String areaCode = request.getParameter("areacode");
				list = runner
						.query(connection,
								"select  subarea_code as code,subarea_name as name from basic_subarea where area_code=?",
								handler, areaCode);
				break;
			case "ps":
				String areacode = request.getParameter("areacode");
				String subAreaCode = request.getParameter("subareacode");
				String sqlString = "select ps_code  as code,ps_name as name from basic_policestation where area_code=? and subarea_code=? ";
				list = runner.query(connection, sqlString, handler, areacode,
						subAreaCode);
				break;
			case "netbar":
				String acode = request.getParameter("areacode");
				String pscode = request.getParameter("pscode");
				String sqlString2 = "select netbar_code as code ,netbar_name as name from basic_netbar where area_code=? and ps_code=? ";
				list = runner.query(connection, sqlString2, handler, acode,
						pscode);
				break;
			default:
				break;
			}
			String addSelect = request.getParameter("addselect");
			if (addSelect != null) {
				if (addSelect.equals("1")) {
					list.add(0, new CommonBean("0", "--请选择--"));
				}
			}
			Gson gson = new Gson();
			resultString = gson.toJson(list);
		} catch (Exception e) {
			// TODO: handle exception
			resultString = e.getMessage();
		} finally {
			try {
				DbUtils.close(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return resultString;
	}

}
