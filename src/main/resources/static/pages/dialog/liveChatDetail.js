
function showLiveChatDetailDialog(liveDate, id, currentLiveInfo){
    const size = getWindowSize();
    const $ = layui.jquery;
    const table = layui.table;
    const url = getLiveUrl(currentLiveInfo, liveDate);
    showDialog("dialog/liveChatDetail.html", {
        title: "附近弹幕",
        area: [Math.min(800, size[0]) + 'px', Math.min(800, size[1]) + 'px'],
        success: function (layero){
            const cols = [[
                {field: 'liveDate', width: 120, title: '日期', sort: true, templet: (d, e)=>{
                        const content = d.liveDate;
                        const currentId = d.id;
                        if (id == currentId){
                            return `<span class="selected">${content}</span>`;
                        }
                        return content;
                    }},
                {field: 'authorName', width: 180, title: '用户名', templet: (d)=>{
                        if(d.authorImage){
                            return `<div><img alt="" class="header" src="${d.authorImage}=s32-c-k-c0x00ffffff-no-rj">${d.authorName}</div>`;
                        }
                        return `<div><img alt="" class="twitch" src="https://twitch.tv/favicon.ico">${d.authorName}</div>`;
                    }},
                {field: 'message', minWidth: 180, title: '消息', templet: (d)=>{
                        let message = d.message || '';
                        if(d.emotesCount){
                            const span_id = Math.random().toString(36);
                            message = '<span id="' + span_id + '">' + message + '</span>';
                            getEmoteMessage(d.message).then(result => document.getElementById(span_id).innerHTML = result);
                        }
                        const scInfo = d.scInfo;
                        if(scInfo){
                            const index = scInfo.indexOf("#");
                            if(index > -1){
                                const color = scInfo.substring(index, index + 7);
                                if(d.scAmount){
                                    message = '<span style="color: '+ color +'">('+ d.scAmount +') '+ message +'</span>';
                                }
                            }
                        }
                        return message;
                    }},
                {field: 'timeText', width: 90, title: '时间', sort: true, align: "right", templet: (d)=>{
                        const sendDateTime = formatTime(d.timestamp / 1000);
                        const title = "发送时间：" + sendDateTime;
                        const timeText = d.timeText;
                        if (timeText){
                            return getUrlTag(getLiveUrl(currentLiveInfo, d.liveDate), timeText, title);
                        }
                        const sendTime = sendDateTime.substring(11);
                        return `<span title="${title}">${sendTime}</span>`;
                    }}
            ]];
            table.render({
                elem: '#liveChatDetail',
                method:'post',
                cols: cols,
                loading: true,
                url: '../liveChat/queryLiveChatDetail',
                where: {
                    liveDate: liveDate,
                    id: id,
                    schema: layui.data("navInfo")["schema"]
                },
                done: function(res){
                    $("#liveChatDetailArea span.selected").closest("tr").css("background-color", "#ddd");
                },
                page: true,
                limits: [20, 30, 50, 100],
                limit: 20,
            });
            table.on('rowDouble(liveChatDetailTableFilter)', tr => {
                const data = tr.data;
                id = data.id;
                table.reload('liveChatDetail', {
                    where: {
                        liveDate: liveDate,
                        id: id,
                        schema: layui.data("navInfo")["schema"]
                    }
                });
                return false;
            });

        },
        btn: ["关闭"]
    })
}