
function showSrtDetailDialog(liveDate, serial, currentLiveInfo){
    const size = getWindowSize();
    const $ = layui.jquery;
    const table = layui.table;
    const url = getLiveUrl(currentLiveInfo, liveDate);
    showDialog("dialog/srtDetail.html", {
        title: "附近字幕",
        area: [Math.min(800, size[0]) + 'px', Math.min(800, size[1]) + 'px'],
        success: function (layero){
            const cols = [[
                {field: 'liveDate', width: 120, title: '日期', sort: true},
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
                    serial: serial,
                    schema: layui.data("navInfo")["schema"]
                },
                done: function(res){
                    $("#srtDetailArea span.selected").closest("tr").css("background-color", "#ddd");
                },
                page: true,
                limits: [20, 30, 50, 100],
                limit: 20,
            });
            table.on('rowDouble(srtDetailTableFilter)', tr => {
                const data = tr.data;
                serial = data.serial;
                table.reload('srtDetail', {
                    where: {
                        liveDate: liveDate,
                        serial: serial,
                        schema: layui.data("navInfo")["schema"]
                    }
                });
                return false;
            });

        },
        btn: ["关闭"]
    })
}