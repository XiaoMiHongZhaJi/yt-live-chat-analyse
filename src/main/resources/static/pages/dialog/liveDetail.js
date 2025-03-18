
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
                $(".detail").each((i, e) => {
                    const span = $(e);
                    const name = span.data("name");
                    let value = data[name];
                    if(value) {
                        if (name == "viewCount" || name == "likeCount") {
                            span.text(formatNum(value));
                        } else if (name == "timeline") {
                            //时间线处理
                            span.html(getTimeLine(marked.marked(value), data["url"]));
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
                $(".timeline img").each((i, e) => {
                    const originalUrl = $(e).data("src");
                    $.ajax({
                        url: "../liveInfo/queryImgUrl",
                        data: { originalUrl: originalUrl },
                        success: function (url) {
                            if(url){
                                $(e).after(`<button type="button" class="layui-btn layui-btn-normal layui-btn-radius" lay-on="showPhoto" data-url="${url}">查看图片</button>`);
                            }else{
                                $(e).after('图片已失效');
                            }
                        },
                        error: function (xhr, status, error) {
                            console.error("Failed to get image URL. Status:", xhr.status, error);
                        }
                    });
                })
            },
            btn: ["关闭"]
        })
        layui.util.on({
            showPhoto: function (obj) {
                layer.photos({
                    photos: {
                        "data": [{"alt": data.liveDate + "_思维导图", "src": obj.data("url")}]
                    },
                    footer: true
                });
            }
        })
    })
}

function getTimeLine(timeline, url){
    const $ = layui.jquery;
    let html = '';
    $(timeline.split("\n")).each((i, e)=>{
        let line = '';
        $(e.replace("\t", " ").split(" ")).each((j, content)=>{
            if(content.indexOf("src") > -1){
                line += "data-" + content;
            }else if(content && content.indexOf(":") > -1){
                const index = content.indexOf(">");
                line += content.substring(0, index + 1);
                const timeString = content.substring(index + 1)
                if(!isNaN(timeString[0])){
                    line += getYtUrlTag(url, timeString);
                }else{
                    line += timeString;
                }
            }else{
                line += content;
            }
            line += " ";
        })
        html += line;
    })
    return html;
}