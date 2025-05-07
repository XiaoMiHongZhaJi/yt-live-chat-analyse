
const defaultYear = "2025";
const yearList = [2024, 2023, 2022, 2021];
let liveInfoDict = {};

function initYearNav() {
    layui.use(['jquery'], function(){
        const $ = layui.jquery;
        const currentYear = layui.data("navInfo")["year"];
        if(!currentYear){
            layui.data("navInfo",{
                key: "year",
                value: defaultYear
            });
        }else if(yearList.indexOf(currentYear) > -1){
            $(".layui-nav").append('<li class="layui-nav-item layui-nav-year" data-year="' + defaultYear + '"><a href="liveChat.html">' + defaultYear + '</a></li>');
        }
        for (let i = 0; i < yearList.length; i++) {
            const year = yearList[i];
            if(currentYear != year){
                $(".layui-nav").append('<li class="layui-nav-item layui-nav-year" data-year="' + year + '"><a href="liveChat.html">' + year + '</a></li>');
            }
        }
        $(".layui-nav-year").click((i) => {
            const year = $(i.target).closest("li").data("year");
            layui.data("navInfo",{
                key: "year",
                value: year
            });
        })
    })
    bindTripleClick();
}

let db;
const DB_NAME = "emoteDB";
const DB_VERSION = 1;
const STORE_NAME = "emotes";

// 打开IndexedDB
function openDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION);
        request.onupgradeneeded = (event) => {
            db = event.target.result;
            if (!db.objectStoreNames.contains(STORE_NAME)) {
                db.createObjectStore(STORE_NAME, { keyPath: "name" });
            }
        };
        request.onerror = (event) => {
            reject("打开数据库失败");
        };
        request.onsuccess = (event) => {
            db = event.target.result;
            resolve(db);
        };
    });
}

// 获取指定名称的表情，若数据未准备好则返回null
function getEmoteByName(name) {
    if (!db) {
        return null;  // 如果数据库尚未打开，返回null
    }
    const transaction = db.transaction(STORE_NAME, "readonly");
    const store = transaction.objectStore(STORE_NAME);
    const request = store.get(name);  // 获取特定名称的表情
    let emote = null;
    // 同步等待数据返回
    request.onsuccess = (event) => {
        emote = event.target.result;  // 数据存在，返回表情对象
    };
    // 如果查询失败或数据不存在，返回null
    request.onerror = () => {
        emote = null;
    };
    // 返回数据或者null
    return emote;
}

// 向数据库中插入或更新表情数据
function saveEmote(emote) {
    return new Promise((resolve, reject) => {
        const transaction = db.transaction(STORE_NAME, "readwrite");
        const store = transaction.objectStore(STORE_NAME);
        const request = store.put(emote);
        request.onsuccess = () => {
            resolve();
        };
        request.onerror = (event) => {
            reject("保存表情数据失败");
        };
    });
}

// 主逻辑
layui.use(['jquery'], function() {
    const $ = layui.jquery;
    const layer = layui.layer;
    openDB().then(() => {
        // 获取数据字典
        let emoteDict = {};
        // 检查是否有STORAGE_OK
        let storageOkEmote = layui.data("emoteDict");
        if (!storageOkEmote || !storageOkEmote["STORAGE_OK"] || storageOkEmote["STORAGE_OK"] != "2") {
            // 如果没有STORAGE_OK标记，则进行ajax请求获取数据
            $.ajax({url: '../liveChat/queryEmotes'}).then(data => {
                $(data).each((i, emote) => {
                    emoteDict[emote.name] = emote;
                    saveEmote(emote);  // 存储到IndexedDB
                });
                layui.data("emoteDict",null);
                // 最后更新STORAGE_OK标志
                layui.data("emoteDict",{
                    key: "STORAGE_OK",
                    value: "2"
                });
            });
            layer.msg("正在初始化一些数据，请稍候...");
        }
    }).catch(err => {
        console.error(err);
    });
});

function getEmoteMssage(message){
    let realMessage;
    if(message.indexOf(":") > -1){
        //YouTube
        realMessage = message.substring(0,message.indexOf(":"));
        let remain = message.substring(message.indexOf(":") + 1);
        while(remain && remain.indexOf(":") > -1){
            const key = remain.substring(0, remain.indexOf(":"));
            const emote = getEmoteByName(key);
            if(emote){
                const emotesId = emote.emotesId;
                if(emote.isCustomEmoji){
                    realMessage += '<img alt="" class="emote" src="'+ emote.images +'">';
                }else{
                    realMessage += emotesId;
                }
            }else if(key.trim()){
                realMessage += "[" + key + "]";
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
            const emote = getEmoteByName(split);
            if(emote){
                realMessage += '<img class="emote" src="'+ emote.images +'">';
            }else if(split.trim()){
                if(/^[a-zA-Z ]+$/.test(split)){
                    realMessage += "[" + split + "]";
                }else{
                    realMessage += " " + split + " ";
                }
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
            skin: 'layui-layer-rim', //加上边框
            shade: 0.3,
            shadeClose: true,
            content: str,
            btn: ['关闭'],
            btnAlign: 'c'
        };
        settings = $.extend({}, def, settings);
        layer.open(settings);
    });
}
function getYtUrl(url, time){
    if(!time){
        return url;
    }
    if(isNaN(time)){
        time = time.trim();
        let negative = false;
        if(time.indexOf("-") == 0){
            negative = true;
            time = time.substring(1);
        }
        const split = time.split(":");
        if(split.length == 2){
            time = parseInt(split[0]) * 60 + parseInt(split[1]);
        }else if(split.length == 3){
            time = parseInt(split[0]) * 3600 + parseInt(split[1]) * 60 + parseInt(split[2]);
        }
        if(negative){
            time = 0 - time;
        }
    }
    if(time > 0){
        url = url + "&t="+ time +"s";
    }
    return url;
}
function getLiveUrl(currentLiveInfo, liveDate){
    let url = '';
    if (currentLiveInfo){
        url = currentLiveInfo.url;
    } else {
        const liveInfo = liveInfoDict[liveDate];
        if (liveInfo){
            url = liveInfo.url;
        }
    }
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
    if(url){
        return '<a target="_blank" href='+ url +'>'+ title +'</a>';
    }
    return title;
}
function getUrlTag(url, time, title){
    if(!url){
        return time;
    }
    if(title){
        title = `title="${title}"`;
    }else{
        title = "";
    }
    if(url.indexOf("youtube") == -1){
        return `<a ${title} target="_blank" href="${url}">${time}</a>`;
    }
    url = getYtUrl(url, time);
    return `<a ${title} target="_blank" href="${url}">${time}</a>`;
}
function initLiveDateSelector(callback, showAll, param){
    const $ = layui.jquery;
    const form = layui.form;
    const currentYear = layui.data("navInfo")["year"];
    if(currentYear && currentYear != defaultYear){
        if(!param){
            param = {};
        }
        param["liveDate"] = currentYear;
    }
    $.ajax({url: '../liveInfo/queryListBySelector', data: param}).then((selectorList)=>{
        if(!selectorList || selectorList.length == 0){
            layer.msg("暂无弹幕数据");
            return;
        }
        $("#liveDate").empty();
        if(showAll){
            $("#liveDate").append('<option value="">全部</option>');
        }
        $(selectorList).each((i, liveInfo) => {
            const liveDate = liveInfo.liveDate;
            let title = liveDate;
            if(liveInfo.title){
                title += "_" + liveInfo.title;
            }
            const option = $('<option value="' + liveDate + '">' + title + '</option>');
            if(i == 0){
                option.prop("selected",true);
            }
            option.data("liveInfo", liveInfo);
            $("#liveDate").append(option);
            liveInfoDict[liveDate] = liveInfo;
        })
        if(selectorList[0]){
            form.render();
            if(callback){
                callback(selectorList[0]);
            }
        }
    }, () => {
        layer.msg("系统出错，请检查数据库连接状态是否正常");
    })
}
function formatNum(string){
    if(string){
        if(isNaN(string)){
            return string;
        }
        return parseInt(string).toLocaleString();
    }
    return "-";
}
function toNum(string){
    if(string){
        return string.replace(/[-, ]/g, "");
    }
    return "";
}
function formatTime(timestamp, offsetStr) {
    // 自动识别时间戳单位（秒 / 毫秒 / 微秒）
    let msTimestamp;
    if (timestamp > 1e15) {
        msTimestamp = Math.floor(timestamp / 1000); // 微秒 → 毫秒
    } else if (timestamp > 1e12) {
        msTimestamp = timestamp; // 已是毫秒
    } else if (timestamp > 1e9) {
        msTimestamp = timestamp * 1000; // 秒 → 毫秒
    } else {
        console.warn('时间戳过小或不合法');
        return '';
    }
    if (offsetStr) {
        const parts = offsetStr.split(':').map(Number);
        let offsetSeconds = 0;
        if (parts.length === 2) {
            offsetSeconds = parts[0] * 60 + parts[1];
        } else if (parts.length === 3) {
            offsetSeconds = parts[0] * 3600 + parts[1] * 60 + parts[2];
        } else {
            console.warn('偏移格式应为 MM:SS 或 HH:MM:SS');
        }
        msTimestamp += offsetSeconds * 1000;
    }
    const date = new Date(msTimestamp);
    const pad = n => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ` +
        `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}
function bindTripleClick(element, callback, interval = 500) {
    const dateLabel = document.getElementById('date-label');
    if(!dateLabel){
        return;
    }
    let clickCount = 0; // 点击次数
    let lastClickTime = 0; // 上次点击时间
    dateLabel.addEventListener('click', () => {
        const currentTime = Date.now();
        // 如果时间间隔超过设定值，重置计数器
        if (currentTime - lastClickTime > interval) {
            clickCount = 0;
        }
        clickCount++;
        lastClickTime = currentTime;
        // 当达到三次点击时，触发回调
        if (clickCount === 3) {
            clickCount = 0; // 重置计数器
            layui.layer.msg('你触发了三击事件！');
            location.href = "liveInfo.html";
        }
    });
}