
function showLiveDetailDialog(liveInfo){
    const size = getWindowSize();
    const table = layui.table;
    const $ = layui.jquery;
    const form = layui.form;
    let height = null;
    $.ajax({url: '../liveInfo/queryLiveInfo', data: {id: liveInfo.id}}).then(data => {
        if(data.timeline){
            height = Math.min(650, size[1]) + 'px';
        }
        showDialog("dialog/liveDetail.html", {
            title: "开播详情",
            area: [null, height],
            success: function(layero){
                $(".live-detail-div .detail").each((i, e) => {
                    const span = $(e);
                    const name = span.data("name");
                    let value = data[name];
                    if(value) {
                        if (name == "viewCount" || name == "likeCount") {
                            span.text(formatNum(value));
                        } else if (name == "timeline") {
                            //时间线处理
                            span.html(getTimeLine(value, data["url"]));
                        } else if (name == "url") {
                            span.html('<a target="_blank" href="' + value + '">' + value + '</a>');
                        } else {
                            span.text(value);
                        }
                    }else if(name == "chatCount"){
                        const liveChatCount = data["liveChatCount"];
                        const livingChatCount = data["livingChatCount"];
                        if(liveChatCount && livingChatCount){
                            value = formatNum(liveChatCount) + ' / <span style="color: blue;">' + formatNum(livingChatCount) + '</span>';
                        }else if(liveChatCount){
                            value = formatNum(liveChatCount);
                        }else{
                            value = '<span style="color: blue;">' + formatNum(livingChatCount) + '</span>';
                        }
                        span.html(value);
                    }else{
                        span.text("-");

                    }
                })
            },
            btn: ["关闭"]
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