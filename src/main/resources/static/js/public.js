let emoteDict;
layui.use(['jquery'], function(){
    const $ = layui.jquery;
    emoteDict = layui.data("emoteDict");
    if(!emoteDict["STORAGE_OK"]){
        $.ajax({url: '/liveChat/queryEmotes'}).then((data) => {
            $(data).each((i,emote)=>{
                emoteDict[emote.name] = emote;
                layui.data("emoteDict",{
                    key: emote.name,
                    value: emote
                });
            })
            emoteDict["STORAGE_OK"] = "1";
            layui.data("emoteDict",{
                key: "STORAGE_OK",
                value: "1"
            });
        })
    }
})
function getEmoteMssage(message){
    let realMessage;
    if(message.indexOf(":") > -1){
        //YouTube
        realMessage = message.substring(0,message.indexOf(":"));
        let remain = message.substring(message.indexOf(":") + 1);
        while(remain && remain.indexOf(":") > -1){
            const key = remain.substring(0, remain.indexOf(":"));
            const emote = emoteDict[key];
            if(emote){
                const emotesId = emote.emotesId;
                if(emote.isCustomEmoji || emotesId == "?"){
                    realMessage += '<img class="emote" src="'+ emote.images +'">';
                }else{
                    realMessage += emotesId;
                }
            }else{
                realMessage += key;
            }
            remain = remain.substring(remain.indexOf(":") + 1);
        }
        realMessage += remain;
        return realMessage;
    }else{
        //twitch
        realMessage = "";
        const $ = layui.jquery;
        $(message.split(" ")).each((i,split)=>{
            const emote = emoteDict[split];
            if(emote){
                realMessage += '<img class="emote" src="'+ emote.images +'">';
            }else {
                realMessage += split;
            }
        })
        return realMessage;
    }
}
function getWindowSize(){
    const $ = layui.jquery;
    const clienWidth = $(window).width();
    const clientHeight = $(window).height();
    return [clienWidth, clientHeight];
}
function showDialog(url, settings){
    const $ = layui.jquery;
    $.get(url, {}, function(str){
        if(!str){
            return;
        }
        const def = {
            type: 1,
            skin: 'layui-layer-rim', //????????????
            shade: 0.3,
            shadeClose: true,
            content: str,
            btn: ['??????'],
            btnAlign: 'c'
        };
        settings = $.extend({}, def, settings);
        layer.open(settings);
    });
}
function getYtUrl(url, time){
    if(isNaN(time)){
        const split = time.split(":");
        if(split.length == 2){
            time = parseInt(split[0]) * 60 + parseInt(split[1]);
        }else if(split.length == 3){
            time = parseInt(split[0]) * 3600 + parseInt(split[1]) * 60 + parseInt(split[2]);
        }
    }
    url = url + "&t="+ time +"s";
    return url;
}
function secondToString(second){
    let minute = second / 60;
    const seconds = second % 60;
    const hour = minute / 60;
    minute = minute % 60;
    if (hour > 0){
        return hour + ":" + (minute < 10 ? "0" : "") + minute + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
    return minute + ":" + (seconds < 10 ? "0" : "") + seconds;
}
function getATag(url, title){
    return '<a target="_blank" href='+ url +'>'+ title +'</a>';
}
function getYtUrlTag(url, time){
    url = getYtUrl(url, time);
    return '<a target="_blank" href='+ url +'>'+ time +'</a>';
}
function initLiveDateSelector(callback, showAll){
    const $ = layui.jquery;
    const form = layui.form;
    $.ajax({url: '/liveInfo/queryListBySelector'}).then((data)=>{
        if(!data || data.length == 0){
            layer.msg("??????????????????");
            return;
        }
        $("#liveDate").empty();
        if(showAll){
            $("#liveDate").append('<option value="">??????</option>');
        }
        $(data).each((i, liveInfo) => {
            const liveDate = liveInfo.liveDate;
            let title = liveDate;
            if(liveInfo.title){
                title = liveDate + "_" + liveInfo.title;
            }
            const option = $('<option value="' + liveDate + '">' + title + '</option>');
            if(i == 0){
                option.prop("selected",true);
            }
            option.data("liveInfo", liveInfo);
            $("#liveDate").append(option);
        })
        if(data[0]){
            form.render();
            if(callback){
                callback(data[0]);
            }
        }
    }, () => {
        layer.msg("?????????????????????????????????????????????????????????");
    })
}
function formatNum(number){
    if(number){
        if(isNaN(number)){
            return number;
        }
        return parseInt(number).toLocaleString();
    }
    return "-";
}