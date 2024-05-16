function showFormData($, layero, data) {
    $(layero).find(".live-detail-div :input").each((i, e) => {
        const input = $(e);
        const name = input.attr("name");
        let value = data[name];
        if (value) {
            if (name.indexOf("Count") > -1) {
                value = formatNum(value);
            }
            input.val(value);
        }
    })
    let startTimestamp = data.startTimestamp
    if(startTimestamp){
        startTimestamp = parseInt(startTimestamp / 1000);
        const startTime = new Date(startTimestamp).toLocaleString("zh-CN");
        $(layero).find("input[name='startTime']").val(startTime.replace(/\//g, "-"));
        $(layero).find("input[name='startTimestamp']").val(parseInt(startTimestamp / 1000));
    }
}

function showLiveDetailEditDialog(liveInfo){
    const size = getWindowSize();
    const table = layui.table;
    const $ = layui.jquery;
    const form = layui.form;
    const laydate = layui.laydate;
    let height = null;
    $.ajax('../liveInfo/queryLiveInfo', {data: {id: liveInfo.id}}).then(data => {
        showDialog("dialog/liveDetailEdit.html", {
            title: "开播详情",
            area: [null, height],
            success: function(layero){
                $(layero).find(".getInfo").click(() => {
                    if($(this).hasClass("layui-btn-disabled")){
                        return;
                    }
                    $(layero).find(".getInfo").addClass("layui-btn-disabled");
                    const url = $(layero).find("input[name='url']").val();
                    if(!url){
                        layer.msg("请先输入URL");
                        return;
                    }
                    $.ajax('../liveInfo/getLiveInfo', {data: {url: url}}).then(data => {
                        if(!data || !data.title){
                            layer.msg("获取信息失败，请尝试更换节点或稍后再试");
                            return;
                        }
                        showFormData($, layero, data);
                        layer.msg("已获取最新数据");
                        $(layero).find(".getInfo").removeClass("layui-btn-disabled");
                    }, () => {
                        layer.msg("获取信息出错");
                        $(layero).find(".getInfo").removeClass("layui-btn-disabled");
                    })
                });
                showFormData($, layero, data);
                form.render();
                laydate.render({
                    elem: '#startTime',
                    type: 'datetime'
                });
            },
            btn: ["更新", "关闭"],
            yes: function (){
                const liveInfo = form.val("live-info-form");
                for(key in liveInfo){
                    if(key.indexOf("Count") > -1){
                        liveInfo[key] = toNum(liveInfo[key]);
                    }
                }
                if(liveInfo.startTimestamp){
                    liveInfo.startTimestamp = liveInfo.startTimestamp * 1000 * 1000;
                }else if(liveInfo.startTime){
                    liveInfo.startTimestamp = new Date(liveInfo.startTime) * 1000;
                }
                $.ajax({
                    url: '../liveInfo/updateLiveInfo',
                    data: liveInfo,
                    method: 'post'
                }).then(() => {
                    layer.closeAll();
                    layer.msg("更新成功");
                    table.reload('liveInfo');
                }, () => {
                    layer.msg("更新失败");
                })
            }
        })
    })
}