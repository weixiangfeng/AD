package xf.ad.action;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import xf.ad.model.UrlBean;
import xf.ad.service.Tools;

import com.google.gson.Gson;

public class ShowUrlAction implements IAction {
	@Override
	public String Excute(HttpServletRequest request,
			HttpServletResponse response) {
		// 根据传递的参数查询一段时间范围内
		Connection connection = null;
		String resultString = null;
		try {
			connection = Tools.createConnection();
			QueryRunner runner = new QueryRunner();
			ResultSetHandler<List<UrlBean>> urlHandler = new BeanListHandler<UrlBean>(
					UrlBean.class);
			// 获取参数
			String city = request.getParameter("city");
			String code = request.getParameter("code");
			String type = request.getParameter("type");
			String name = request.getParameter("showname");
			String ad = request.getParameter("ad");
			String subad = request.getParameter("subad");
			String st = request.getParameter("bgt");
			String et = request.getParameter("edt");
			String order = request.getParameter("order");
			String sort = request.getParameter("sort");

			if (code == null || type == null) {
				return "param error";
			}

			String showName = URLDecoder.decode(name, "utf-8");
			
			Object[] adArray = runner
					.query(connection,
							"select ad_code,ad_name,ad_table from basic_ad_type where ad_code=?",
							new ArrayHandler(), ad);
			String urlColumnName = "url";
			String groupByColumnName = "url";
			String showCountColumn = "sum(count)as showcount";
			String ynCountColumn = " count";
			String adCode = adArray[0].toString();
			String adName = (String) adArray[1];
			String adTable = (String) adArray[2];

			if (adCode.equals("3") || adCode.equals("9")) {
				urlColumnName = "url ||' '|| url2 as url";
				groupByColumnName = "url,url2";
			}
			if (adCode.equals("2") || adCode.equals("3")) {
				if (subad.equals("0")) {
					showCountColumn = "sum(ncount) as showcount ";
					ynCountColumn = " ncount";
				}
			}
			// 获取排序及分页参数
			int page = Integer.parseInt(request.getParameter("page"));
			int rows = Integer.parseInt(request.getParameter("rows"));
			int startRecord = (page - 1) * rows + 1;
			int endRecord = page * rows;
			String wheresString = "  where area_code='" + city + "'";
			if (type.equals("bar")) {
				wheresString += " and  bar_code='" + code + "' ";
			} else if (type.equals("area")) {
				wheresString += " and  subarea_code='" + code + "' ";
			}
			wheresString += " and stats_time between '" + st + "' and '" + et
					+ "' and  " + ynCountColumn + " >0   group by  "
					+ groupByColumnName;
			;

			String sqlString = "select '" + showName + "' as showName,'"
					+ adName + "' as adName," + urlColumnName + ","
					+ showCountColumn + " from " + adTable + wheresString
					+ "  order by   " + sort + "   " + order;

			String sortSqlString = "SELECT * FROM ( SELECT A.*, ROWNUM RN FROM  ( "
					+ sqlString
					+ " ) A WHERE ROWNUM <= "
					+ endRecord
					+ " ) WHERE RN >= " + startRecord + "";
			List<UrlBean> urlList = runner.query(connection, sortSqlString,
					urlHandler);

			String totalCountSqlString = "select count(*) from ( " + sqlString
					+ " )";
			Object count = runner.query(connection, totalCountSqlString,
					new ScalarHandler<>());

			int size = Integer.parseInt(count.toString());
			Gson gson = new Gson();
			String jsonString = gson.toJson(urlList);
			StringBuilder jsonBuilder = new StringBuilder();
			jsonBuilder.append("{\"total\":\"" + size + "\",");
			jsonBuilder.append("\"rows\":");
			jsonBuilder.append(jsonString);
			jsonBuilder.append("}");
			resultString = jsonBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}
}
