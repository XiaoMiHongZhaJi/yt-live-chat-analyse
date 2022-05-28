
function showHotListDetailDialog(liveDate, startTime, intervalMinutes, url, tr){
    const size = getWindowSize();
    const $ = layui.jquery;
    const table = layui.table;
    showDialog("/pages/dialog/hotListDetail.html", {
        title: "弹幕详情",
        area: [Math.min(800, size[0]) + 'px', Math.min(600, size[1]) + 'px'],
        success: function (layero){
            $(layero).find("#single").html(tr.html());
            $(layero).find("#single").find("td[data-field='messages']>div").removeClass().css({
                "padding": "0 15px",
                "line-height": "28px"
            });
            table.render({
                elem: '#hotListDetail',
                initSort: {
                    field: 'timeText', //排序字段，对应 cols 设定的各字段名
                    type: 'asc' //排序方式  asc: 升序、desc: 降序、null: 默认排序
                },
                method:'post',
                cols: [[
                    {field: 'row', width: 50, title: '序号', type: 'numbers'},
                    {field: 'authorName', width: 180, title: '用户名',templet: (d)=>{
                        if(d.authorImage){
                            return '<div><img class="header" src="'+ d.authorImage +'=s32-c-k-c0x00ffffff-no-rj">'+ d.authorName +'</div>'
                        }
                        return d.authorName;
                    }},
                    {field: 'message', minWidth: 180, title: '消息',templet: (d)=>{
                            const message = d.message;
                            if(message && d.emotesCount){
                            return getEmoteMssage(message);
                        }
                        return message;
                    }},
                    {field: 'timeText', width: 90, title: '时间', sort: true, align: "right",templet: (d)=>{
                            const timeText = d.timeText;
                            if(url && timeText){
                            return getYtUrlTag(url, timeText);
                        }
                        return timeText;
                    }}
                ]],
                loading: true,
                done: function(res){

                },
                url: '/analyse/queryHotListDetail',
                where: {liveDate, startTime, intervalMinutes}
            });
        },
        btn: ["关闭"]
    })
}