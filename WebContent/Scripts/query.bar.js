var StartTime = '';
var EndTime = '';
var City = '';
var Area = '';
var AreaName = '';
var Flag = 'load';
var BarNo = '';
var BarName = '';
var test = null;
$(function() {
			InitDate();
			if (GetQueryString("bgt") != null) {
				StartTime = unescape(GetQueryString("bgt"));
			}
			if (GetQueryString("edt") != null) {
				EndTime = unescape(GetQueryString("edt"));
			}
			City = unescape(GetQueryString("city"));
			Area = unescape(GetQueryString("area"));
			AreaName = unescape(GetQueryString("areaname"));
			LoadArea();
			// $('#dateStart').datebox('setValue', StartTime);
			// $('#dateEnd').datebox('setValue', EndTime);
			// 因为unescape函数返回字符类型
			if (City != 'null' && StartTime != 'null') {
				LoadData();
			}
			$('#btnQuery').click(Query);
		});
function Query() {
	City = $('#ccArea').combobox('getValue');
	Area = $('#ccSubArea').combobox('getValue');
	BarNo = $.trim($('#barNo').val());
	BarName = encodeURI($.trim($('#barName').val()));
	StartTime = $('#dateStart').datebox('getValue');
	EndTime = $('#dateEnd').datebox('getValue');
	Flag = 'query';
	LoadData();
}

function LoadData() {
	$.messager.progress({
				title : '请您稍等',
				msg : '正在处理,请稍等...'
			});
	$.post('/wxf/ActionService?action=bar', {
				city : City,
				area : Area,
				st : StartTime,
				et : EndTime,
				flag : Flag,
				barno : BarNo,
				barname : BarName
			}, function(data) {
				$.messager.progress('close');
				var json = $.parseJSON(data);
				ShowContent(json);
				$("#queryTable").stupidtable();
			});
}

function ShowContent(json) {
	var THead = '<thead><tr><th>网吧</th><th data-sort="int">装机量</th><th data-sort="int">上机人次</th><th data-sort="int">活跃终端数</th>';
	// var THead = "";
	var JsonHead = json.head;
	for (var c in JsonHead) {
		// 主页展现和搜索分为指定的和未指定的
		if (JsonHead[c].name == '主页访问' || JsonHead[c].name == '主页搜索') {
			THead += '<th data-sort="int">指定' + JsonHead[c].name
					+ '</th><th data-sort="int">非指定' + JsonHead[c].name
					+ '</th>';
		} else {
			THead += '<th  data-sort="int">' + JsonHead[c].name + '</th>';
		}
	}
	THead += "</tr></thead>";
	var JsonData = json.data;
	var TRs = '<tbody>';
	for (var x in JsonData) {
		var JsonRows = JsonData[x].rows;
		var BasicRows = JsonData[x].basicrows;
		var trsStr = '<tr><td><a href="javascript:void(0)" style="color:blue;" onclick="showBarHour(\''
				+ JsonData[x].barcode
				+ '\',\''
				+ JsonData[x].barname
				+ '\')">'
				+ JsonData[x].barname + '</a></td>';
		var seatscount = '0';
		var onlinecount = '0';
		var terminalcount = '0';
		if (BasicRows[0].seatscount != undefined) {
			seatscount = BasicRows[0].seatscount;
		}
		if (BasicRows[0].onlinecount != undefined) {
			onlinecount = BasicRows[0].onlinecount;
		}
		if (BasicRows[0].terminalcount != undefined) {
			terminalcount = BasicRows[0].terminalcount;
		}
		trsStr += '<td>' + seatscount + '</td><td>' + onlinecount + '</td><td>'
				+ terminalcount + '</td>';

		for (var y in JsonRows) {
			trsStr += '<td><a href="javascript:void(0)" style="color:blue;" onclick="openURL(\''
					+ JsonData[x].barcode
					+ '\',\''
					+ JsonData[x].barname
					+ '\',\''
					+ JsonRows[y].adcode
					+ '\',\''
					+ JsonRows[y].subadcode
					+ '\')"><span style="font-size:16px;">'
					+ JsonRows[y].count
					+ '</span></a></td>';
		}
		trsStr += '</tr>';
		TRs += trsStr;
	}
	var table = THead + TRs + '</tbody>';
	hStr = "<table id='queryTable'>" + table + "</table>";
	$("#queryRecords").html(hStr);
}

function ParseDate(time) {
	if (time != "" && time != undefined) {
		return time.substr(0, 4) + "年" + time.substr(4, 2) + "月"
				+ time.substr(6, 2) + "日";
	} else {
		return "";
	}
}
function openURL(code, showname, ad, subad) {
	var opage = window.open("ShowUrl.html?city=" + City + "&code=" + code
			+ "&type=bar&showname=" + escape(showname) + "&ad=" + ad
			+ "&subad=" + subad + "&bgt=" + escape(StartTime) + "&edt="
			+ escape(EndTime) + "");
	opage.focus();
}
function InitDate() {
	var curDate = new Date();
	var year = curDate.getFullYear();
	var month = curDate.getMonth() + 1;
	var day = curDate.getDate();
	var time = year + '' + (month < 10 ? ('0' + month) : month) + ''
			+ (day < 10 ? ('0' + day) : day);
	$('#dateStart').datebox('setValue', time);
	$('#dateEnd').datebox('setValue', time);
}

function GetQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

function showBarHour(barcode, barname) {
	var opage = window.open("BarHour.html?city=" + City + "&bar=" + barcode
			+ "&barname=" + escape(barname) + "&bgt=" + escape(StartTime)
			+ "&edt=" + escape(EndTime) + "");
	opage.focus();
}

function LoadArea() {
	$('#ccSubArea').combobox({
				width : '133'
			});
	$('#ccArea').combobox({
				url : '/wxf/ActionService?action=combobox&type=area',
				valueField : 'code',
				textField : 'name',
				panelHeight : '400',
				width : '133',
				onChange : function(newValue, oldValue) {
					loadSubArea(newValue);
				},
				editable : false,
				onLoadSuccess : function() {
					var data = $('#ccArea').combobox('getData');
					if (data.length > 0) {
						if (GetQueryString("city") != null) {
							$("#ccArea").combobox('select', City);
						} else {
							$("#ccArea").combobox('select', data[0].code);
						}

					}
				}
			});
}
// 通过地区号去查询分局
function loadSubArea(areaCode) {
	if (areaCode == '0') {
		$('#ccSubArea').combobox('clear');
	} else {
		var paramStr = '&type=subarea&areacode=' + areaCode + '';
		$('#ccSubArea').combobox({
					url : '/wxf/ActionService?action=combobox' + paramStr + '',
					valueField : 'code',
					textField : 'name',
					panelHeight : '300',
					width : '133',
					editable : false,
					onLoadSuccess : function() {
						var data = $('#ccSubArea').combobox('getData');
						if (data.length > 0) {
							if (GetQueryString("area") != null) {
								$("#ccSubArea").combobox('select', Area);
							} else {
								$("#ccSubArea").combobox('select', data[0].code);
							}
						}
					}
				});
	}
}

function Messager(msg) {
	$.messager.alert("信息提醒", msg, "warning");
}
