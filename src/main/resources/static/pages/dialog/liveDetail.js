
function showLiveDetailDialog(liveInfo, showEdit){
    const size = getWindowSize();
    const table = layui.table;
    const $ = layui.jquery;
    const form = layui.form;
    let height = null;
    $.ajax({url: '/liveInfo/queryLiveInfo', data: {id: liveInfo.id}}).then((data) => {
        if(showEdit || data.timeline){
            height = Math.min(650, size[1]) + 'px';
        }
        showDialog("/pages/dialog/liveDetail.html", {
            title: "开播详情",
            area: [null, height],
            success: function(layero){
                if(showEdit){
                    $(layero).find(".getInfo").removeClass("layui-hide");
                    $(layero).find(".live-detail-div :input").each((i, e) => {
                        const input = $(e);
                        if (!input.prop("disabled")){
                            input.next("span.detail").addClass("layui-hide");
                            input.removeClass("layui-hide");
                        }
                    })
                }else{
                    $(layero).find(".layui-layer-btn0").addClass("layui-hide");
                }
                $(layero).find(".getInfo").click(() => {
                    if($(this).hasClass("layui-btn-disabled")){
                        return;
                    }
                    const url = $(layero).find("input[name='url']").val();
                    if(!url){
                        layer.msg("请先输入URL");
                        return;
                    }
                    $.ajax({url: '/liveInfo/getLiveInfo', data: {url: url}}).then((data) => {
                        $(layero).find(".getInfo").addClass("layui-btn-disabled");
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
                    }, () => {
                        layer.msg("获取信息失败");
                    })
                });
                $(".live-detail-div :input").each((i, e) => {
                    const input = $(e);
                    const name = input.attr("name");
                    let value = data[name];
                    if(value){
                        if(name == "viewCount" || name == "likeCount" || name == "chatCount"){
                            value = formatNum(value);
                            input.val(value);
                            input.next("span.detail").text(value);
                        }else if(name == "timeline"){
                            //时间线处理
                            input.val(value);
                            const timeline = getTimeLine(value, data["url"]);
                            input.next("span.detail").html(timeline);
                        }else if(name == "url"){
                            input.val(value);
                            input.next("span.detail").html('<a target="_blank" href="' + value + '">' + value + '</a>');
                        }else{
                            input.val(value);
                            input.next("span.detail").text(value);
                        }
                    }else{
                        input.next("span.detail").text("-");
                        if(name == "timeline" && !showEdit){
                            input.closest(".layui-form-item").addClass("layui-hide");
                        }
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
                    url: '/liveInfo/updateLiveInfo',
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

function getTimeLine(timeline, url){
    const $ = layui.jquery;
    let html = '<p>';
    $(timeline.split("\n")).each((i,e)=>{
        if(!e){
            return;
        }
        $(e.split(" ")).each((j,content)=>{
            if(content && !isNaN(content[0]) && content.indexOf(":") > -1){
                html += getYtUrlTag(url, content);
            }else{
                html += content;
            }
            html += " ";
        })
        html += '</p><p>';
    })
    html += "</p>";
    return html;
}