
function showHotListDetailDialog(liveDate, startTimestamp, intervalMinutes, url, tr){
    const size = getWindowSize();
    const $ = layui.jquery;
    const table = layui.table;
    showDialog("dialog/hotListDetail.html", {
        title: "弹幕详情",
        area: [Math.min(800, size[0]) + 'px', Math.min(800, size[1]) + 'px'],
        success: function (layero){
            $(layero).find("#single").html(tr.html());
            $(layero).find("#single").find("td[data-field='messages']>div").removeClass().css({
                "padding": "0 15px",
                "line-height": "28px"
            });
            table.render({
                elem: '#hotListDetail',
                method:'post',
                cols: [[
                    {field: 'row', width: 50, title: '序号', type: 'numbers'},
                    {field: 'authorName', width: 180, title: '用户名',templet: (d)=>{
                        if(d.authorImage){
                            return '<div><img class="header" src="'+ d.authorImage +'=s32-c-k-c0x00ffffff-no-rj">'+ d.authorName +'<span style="opacity:0">：</span></div>'
                        }
                        return d.authorName;
                    }},
                    {field: 'message', minWidth: 180, title: '消息',templet: (d)=>{
                        let message = d.message || '';
                        if(d.emotesCount){
                            const span_id = Math.random().toString(36);
                            message = '<span id="' + span_id + '">' + message + '</span>';
                            (async () => {
                                const realMessage = await getEmoteMessage(d.message);
                                document.getElementById(span_id).innerHTML = realMessage;
                            })();
                        }
                        const scInfo = d.scInfo
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
                    {field: 'timeText', width: 90, title: '时间', sort: true, align: "right",templet: (d)=>{
                        const timeText = d.timeText;
                        if(timeText){
                            return getUrlTag(url, timeText);
                        }
                        return new Date(d.timestamp / 1000).toTimeString().substring(0, 8);
                    }}
                ]],
                loading: true,
                done: function(res){

                },
                url: '../analyse/queryHotListDetail',
                where: {liveDate, startTimestamp, intervalMinutes}
            });
        },
        btn: ["关闭"]
    })
}