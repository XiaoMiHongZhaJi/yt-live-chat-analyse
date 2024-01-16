
function showLiveDetailEditDialog(liveInfo){
    const size = getWindowSize();
    const table = layui.table;
    const $ = layui.jquery;
    const form = layui.form;
    let height = null;
    $.ajax({url: '../liveInfo/queryLiveInfo', data: {id: liveInfo.id}}).then((data) => {
        if(data.timeline){
            height = Math.min(650, size[1]) + 'px';
        }
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
                    $.ajax({url: '../liveInfo/getLiveInfo', data: {url: url}}).then((data) => {
                        if(!data || !data.viewCount){
                            layer.msg("获取信息失败，请尝试更换节点或稍后再试");
                            return;
                        }
                        if(data.viewCount){
                            $(layero).find("input[name='viewCount']").val(formatNum(data.viewCount));
                        }
                        if(data.likeCount){
                            $(layero).find("input[name='likeCount']").val(formatNum(data.likeCount));
                        }
                        if(data.title){
                            $(layero).find("input[name='title']").val(data.title);
                        }
                        if(data.liveDate && !$(layero).find("input[name='liveDate']").val()){
                            $(layero).find("input[name='liveDate']").val(data.liveDate);
                        }
                        layer.msg("已获取最新数据");
                        $(layero).find(".getInfo").removeClass("layui-btn-disabled");
                    }, () => {
                        layer.msg("获取信息失败");
                        $(layero).find(".getInfo").removeClass("layui-btn-disabled");
                    })
                });
                $(".live-detail-div :input").each((i, e) => {
                    const input = $(e);
                    const name = input.attr("name");
                    let value = data[name];
                    if(value){
                        if(name == "viewCount" || name == "likeCount"){
                            value = formatNum(value);
                        }
                        input.val(value);
                    }
                })
            },
            btn: ["更新", "关闭"],
            yes: function (){
                const liveInfo = form.val("live-info-form");
                const viewCount = liveInfo.viewCount;
                const chatCount = liveInfo.chatCount;
                if(viewCount){
                    liveInfo["viewCount"] = viewCount.replace(/,|-| /g,"");
                }
                if(chatCount){
                    liveInfo["chatCount"] = chatCount.replace(/,|-| /g,"");
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