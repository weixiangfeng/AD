var countColumnName = '次数';
var bgt = GetQueryString("bgt");
var edt = GetQueryString("edt");
var ad = GetQueryString("ad");
var subad = GetQueryString("subad");
var city = GetQueryString("city");
var code = GetQueryString("code");
var type = GetQueryString("type");
var showname = GetQueryString("showname");
$(function() {
			if (code == null || type == null||ad==null) {
				$('#lblTitle').text('亲，参数不正确啊');
				return;
			}
			if (ad == '2') {
				if (subad == '0') {
					countColumnName = '非指定主页访问次数';
				} else {
					countColumnName = '指定主页访问次数';
				}
			}
			if (ad == '3') {
				if (subad == '0') {
					countColumnName = '非指定主页搜索次数';
				} else {
					countColumnName = '指定主页搜索次数';
				}
			}

			var title = showname + '' + ParseDate(bgt) + "至" + ParseDate(edt)
					+ "URL统计结果";
			$('#lblTitle').text(title);
			InitGrid();
			LoadData();
		});

function InitGrid() {
	$('#grid').datagrid({
				nowrap : false, // 数据是否显示在一行里
				singleSelect : true,
				sortName : 'showCount',
				sortOrder : 'desc',
				pageSize : 30,
				columns : [[{
							field : 'showName',
							title : '地市',
							align : 'center',
							width : fixWidth(0.1)
						}, {
							field : 'adName',
							title : '广告类型',
							align : 'center',
							width : fixWidth(0.1)
						}, {
							field : 'url',
							title : 'URL',
							align : 'center',
							width : fixWidth(0.5)
						}, {
							field : 'showCount',
							title : countColumnName,
							align : 'center',
							width : fixWidth(0.3),
							sortable : true
						}]],
				pagination : true,
				rownumbers : true,
				fitColumns : true,
				fit : true
			});
}

function ParseDate(time) {
	if (time != "" && time != undefined) {
		return time.substr(0, 4) + "年" + time.substr(4, 2) + "月"
				+ time.substr(6, 2) + "日";
	} else {
		return "";
	}
}

function LoadData() {
	$("#grid").datagrid({
				pageNumber : 1,
				url : '/wxf/ActionService?action=showurl',
				queryParams : {
					ad : ad,
					subad : subad,
					city:city,
					code : code,
					type : type,
					showname : encodeURI(showname),
					bgt : bgt,
					edt : edt
				}
			});
}

function GetQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

function fixWidth(percent) {
	return (document.body.clientWidth) * percent;
}
