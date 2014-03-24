$(function () {
    $("#loginWindow").window({
        title: '登陆窗口',
        width: 290,
        height: 180,
        modal: true,
        shadow: false,
        closable: false,
        maximizable: false,
        minimizable: false,
        collapsible: false,
        resizable: false
    });

    $("#btnLogin").click(function () {
        var username = document.getElementById("username").value;
        var password = document.getElementById("password").value;
        if (username == '') {
            $.messager.alert('消息', '请输入用户名!', 'error');
            return false;
        }
        if (password == '') {
            $.messager.alert('消息', '请输入密码!', 'error');
            return false;
        }
        loginSys(username, password);
    });


});

//登陆操作
function loginSys(username, password) {

    $.ajax({
        type: "POST",
        dataType: "json",
        url: "/wxf/AreaService",
        data: { action: 'logins', username: username, password: password },
        success: function (dataa) {
            if (dataa.result == "ok") {
                var role = dataa.role;
                window.location.href = "rr.aspx?user="+username+"&role="+role+"";
            }
            else {
                $.messager.alert('错误', dataa.msg, 'error');
            }
        },
        error: function (msg) {
            $.messager.alert('错误', '获取账号信息失败...请联系管理员!', 'error');
        }
    });
}