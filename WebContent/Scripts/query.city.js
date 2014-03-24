var StartTime = '';
var EndTime = '';
$(function() {
			var curDate = new Date();
			var year = curDate.getFullYear();
			var month = curDate.getMonth() + 1;
			var day = curDate.getDate();
			var time = year +''+ (month < 10 ? ('0' + month) : month)
					+''+ (day < 10 ? ('0' + day) : day);
			$('#dateStart').datebox('setValue', time);
			$('#dateEnd').datebox('setValue', time);
			loadCity();
			loadADType();
			$('#btnQuery').click(Query);
		});

function Query() {
	var city = $('#ccCity').combobox('getValue');
	var adtype = $('#ccADType').combobox('getValue');
	StartTime = $('#dateStart').datebox('getValue');
	EndTime = $('#dateEnd').datebox('getValue');
	LoadData(city, adtype);
}

function LoadData(city, adtype) {
	$.messager.progress({
				title : '请您稍等',
				msg : '正在处理,请稍等...'
			});
	$.post('/wxf/ActionService?action=city', {
				city : city,
				adtype : adtype,
				st : StartTime,
				et : EndTime
			}, function(data) {
				$.messager.progress('close');
				var json = $.parseJSON(data);
				ShowContent(json);
			});
}

function ShowContent(json) {
	var THead = "<tr><th>地市</th><th>装机量</th><th>上机人次</th><th>活跃终端数</th>";
	// var THead = "";
	var JsonHead = json.head;
	for (var c in JsonHead) {
		// 主页展现和搜索分为指定的和未指定的
		if (JsonHead[c].name == '主页访问' || JsonHead[c].name == '主页搜索') {
			THead += "<th>指定" + JsonHead[c].name + "</th><th>非指定"
					+ JsonHead[c].name + "</th>";
		} else {
			THead += "<th>" + JsonHead[c].name + "</th>";
		}
	}
	THead += "</tr>";
	var JsonData = json.data;
	var TRs = '';
	for (var x in JsonData) {
		var JsonRows = JsonData[x].rows;
		var BasicRows = JsonData[x].basicrows;
		var trsStr = '<tr><td><a href="javascript:void(0)" style="color:blue;" onclick="showArea(\''
				+ JsonData[x].citycode
				+ '\',\''
				+ JsonData[x].cityname
				+ '\')">' + JsonData[x].cityname + '</a></td>';
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
		trsStr += '<td><a href="javascript:void(0)" style="color:blue;" onclick="openList(\''
				+ JsonData[x].citycode
				+ '\',\''
				+ JsonData[x].cityname
				+ '\')">'
				+ seatscount
				+ '</a></td><td><a href="javascript:void(0)" style="color:blue;" onclick="openList(\''
				+ JsonData[x].citycode
				+ '\',\''
				+ JsonData[x].cityname
				+ '\')">'
				+ onlinecount
				+ '</a></td><td><a href="javascript:void(0)" style="color:blue;" onclick="openList(\''
				+ JsonData[x].citycode
				+ '\',\''
				+ JsonData[x].cityname
				+ '\')">' + terminalcount + '</a></td>';

		for (var y in JsonRows) {
			trsStr += '<td><a href="javascript:void(0)" style="color:blue;" onclick="openURL(\''
					+ JsonData[x].citycode
					+ '\',\''
					+ JsonData[x].cityname
					+ '\',\''
					+ JsonRows[y].adcode
					+ '\',\''
					+ JsonRows[y].subadcode
					+ '\')"><span style="font-size:16px;color:green;">'
					+ JsonRows[y].count + '</span></a></td>';
		}
		trsStr += '</tr>';
		TRs += trsStr;
	}
	var table = THead + TRs;
	hStr = "<table id='queryTable'>" + table + "</table>";
	$("#queryRecords").html(hStr);
}

function loadCity() {
	$('#ccCity').combobox({
				url : '/wxf/ActionService?action=combobox&type=area&addselect=1',
				valueField : 'code',
				textField : 'name',
				panelHeight : '300',
				width : '100',
				editable : false,
				onLoadSuccess : function() {
					var data = $('#ccCity').combobox('getData');
					if (data.length > 0) {
						$("#ccCity").combobox('select', data[0].code);
					}
				}
			});
}
function loadADType() {
	$('#ccADType').combobox({
				url : '/wxf/ActionService?action=combobox&type=adtype&addselect=1',
				valueField : 'code',
				textField : 'name',
				panelHeight : '300',
				width : '100',
				editable : false,
				onLoadSuccess : function() {
					var data = $('#ccADType').combobox('getData');
					if (data.length > 0) {
						$("#ccADType").combobox('select', data[0].code);
					}
				}
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

function openURL(code,showname, ad, subad) {
	var opage = window.open("ShowUrl.html?city="+code+"&code=" + code + "&type=city&showname="+escape(showname)+"&ad=" + ad
			+ "&subad=" + subad + "&bgt=" + escape(StartTime) + "&edt="
			+ escape(EndTime) + "");
	opage.focus();
}

function showArea(citycode, cityname) {
	var opage = window.open("Area.html?city=" + citycode + "&cityname="
			+ escape(cityname) + "&bgt=" + escape(StartTime) + "&edt="
			+ escape(EndTime) + "");
	opage.focus();
}

function openList(citycode, cityname) {
	var opage = window.open("CityDay.html?city=" + citycode + "&cityname="
			+ escape(cityname) + "&bgt=" + escape(StartTime) + "&edt="
			+ escape(EndTime) + "");
	opage.focus();
}
