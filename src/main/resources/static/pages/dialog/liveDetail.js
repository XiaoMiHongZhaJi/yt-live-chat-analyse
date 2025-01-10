
function showLiveDetailDialog(liveInfo){
    const size = getWindowSize();
    const table = layui.table;
    const $ = layui.jquery;
    const form = layui.form;
    let height = null;
    let width = null;
    $.ajax({url: '../liveInfo/queryLiveInfo',
        data: {
            id: liveInfo.id,
            liveDate: liveInfo.liveDate
        }
    }).then(data => {
        if(data.timeline){
            height = Math.min(650, size[1]) + 'px';
            width = Math.min(850, size[0]) + 'px';
        }
        showDialog("dialog/liveDetail.html", {
            title: "开播详情",
            area: [width, height],
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
    let html = '';
    let subtitle = '';
    $(timeline.split("\n")).each((i, e)=>{
        if(!e){
            return ;
        }
        if(e.startsWith("202") && i <= 1 || e == "思维导图"){
            return ;
        }
        if(["关键词", "全文摘要", "章节速览", "要点回顾"].indexOf(e) > -1){
            subtitle = e;
            html += '</div><div class="timeline-title">'
            html += '<p style="font-weight: bold;">' + e + '</p>';
            return ;
        }
        if(e.endsWith("？")){
            subtitle = "？";
            html += '<div><p style="font-weight: bold;">Q: ' + e + '</p>';
            return ;
        }
        if(subtitle == "？"){
            html += '<p>A: ' + e + '</p></div>';
            return ;
        }
        let line = '';
        let bold = false;
        $(e.replace("\t", " ").split(" ")).each((j, content)=>{
            if(content && !isNaN(content[0]) && content.indexOf(":") > -1){
                line += getYtUrlTag(url, content);
                bold = true;
            }else{
                line += content;
            }
            line += " ";
        })
        html += '<p style="' + (bold ? 'font-weight: bold;' : 'text-indent: 2em;') + '">' + line + '</p>';
    })
    html += '</div>';
    return html;
}