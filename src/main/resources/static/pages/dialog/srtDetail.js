
function showSrtDetailDialog(liveDate, serial, url){
    const size = getWindowSize();
    const $ = layui.jquery;
    const table = layui.table;
    let currentLiveInfo = null;
    showDialog("dialog/srtDetail.html", {
        title: "附近字幕",
        area: [Math.min(800, size[0]) + 'px', Math.min(800, size[1]) + 'px'],
        success: function (layero){
            let currentLiveInfo = null;
            const cols = [[
                {field: 'liveDate', width: 120, title: '日期', sort: true, hide: true},
                {field: 'serial', width: 90, title: '序号', sort: true, align: "right"},
                {field: 'startTime', width: 90, title: '时间', sort: true, align: "right", templet: (d)=>{
                        const startTime = d.startTime.split(",")[0];
                        if (startTime){
                            return getUrlTag(url, startTime);
                        }
                        return startTime;
                    }},
                {field: 'content', minWidth: 180, title: '字幕', templet: (d, e)=>{
                        const content = d.content;
                        const currentSerial = d.serial;
                        if (serial == currentSerial){
                            console.log(d,e)
                            return `<span class="selected">${content}</span>`;
                        }
                        return content;
                    }}
            ]];
            table.render({
                elem: '#srtDetail',
                method:'post',
                cols: cols,
                loading: true,
                url: '../srtInfo/querySrtDetail',
                where: {
                    liveDate: liveDate,
                    serial: serial
                },
                done: function(res){
                    $("#srtDetailArea span.selected").closest("tr").css("background-color", "#ddd");
                },
                page: false
            });

        },
        btn: ["关闭"]
    })
}