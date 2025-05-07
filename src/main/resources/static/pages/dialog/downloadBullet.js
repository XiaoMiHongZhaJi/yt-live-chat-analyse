
function showDownloadBulletDialog(downloadBullet){
    const $ = layui.jquery;
    const form = layui.form;
    showDialog("dialog/downloadBullet.html", {
        title: "自定义弹幕样式",
        success: function(layero){
            const liveInfo = $("#liveDate option:selected").data("liveInfo");
            let startTimestamp = liveInfo.startTimestamp;
            if(startTimestamp){
                const startTime = formatTime(startTimestamp);
                $(layero).find("input[name='startTime']").val(startTime);
            }
            form.render();
        },
        btn: ["下载", "取消"],
        yes: function (){
            const config = form.val("downloadBulletForm");
            downloadBullet(config);
        }
    })
}
