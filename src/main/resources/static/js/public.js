
const defaultSchema = "2026";
const schemaList = ["2026", "2025", "2024", "2023", "2022", "2021", "et"];
let liveInfoDict = {};
const DB_NAME = "emoteDB";
const DB_VERSION = 1;
const STORE_NAME = "emotes";

var auth_user = localStorage.getItem("auth_user");

function initSchemaNav() {
    layui.use(['jquery'], function () {
        const $ = layui.jquery;
        const element = layui.element;
        let currentSchema = layui.data("navInfo")["schema"];
        if (!currentSchema) {
            layui.data("navInfo", {key: "schema", value: defaultSchema});
            currentSchema = defaultSchema;
        }
        for (let i = 0; i < schemaList.length; i++) {
            const schema = schemaList[i];
            if (currentSchema != schema) {
                $(".layui-nav").append('<li class="layui-nav-item layui-nav-schema" data-schema="' + schema + '"><a href="liveChat.html">' + schema + '</a></li>');
            }
        }
        $(".layui-nav-schema").click((i) => {
            const schema = $(i.target).closest("li").data("schema");
            layui.data("navInfo", {key: "schema", value: schema});
        })

        // ========== 新增：右侧头像 + 下拉菜单 ==========
        const username = auth_user || "User";
        const firstLetter = username.charAt(0).toUpperCase();
        const bgColor = getColorFromUsername(username);
        let extraMenu = "";
        if (username.toLowerCase() === "admin") {
            extraMenu = `
                <dd><a href="liveInfo.html" class="live-info">数据管理</a></dd>
                <dd><a href="userManage.html" class="user-manage">用户管理</a></dd>
            `;
        }

        // 移除旧头像项
        $(".layui-nav-avatar").remove();

        // 添加 Layui 下拉结构
        $(".layui-nav").append(`
            <li class="layui-nav-item layui-nav-avatar">
                <div class="avatar-circle" style="background-color: ${bgColor};">${firstLetter}</div>
                <dl class="layui-nav-child">
                    <dd><a href="javascript:;" class="username-item" style="color: ${bgColor};">${username}</a></dd>
                    ${extraMenu}
                    <dd><a href="javascript:;" class="change-pwd">修改密码</a></dd>
                    <dd><a href="javascript:;" class="logout">退出登录</a></dd>
                </dl>
            </li>
        `);

        element.render('nav');

        // 修改密码事件
        $(".layui-nav-avatar .change-pwd").on("click", function () {
            showDialog("dialog/changePassword.html", {
                title: "修改密码",
                shadeClose: false,
                btn: ["提交", "取消"],
                success: function (layero){
                    $(layero).find("#userName").val(username);
                    $(document).on('keydown.enterSubmit', function(e) {
                        if (e.key === 'Enter') {
                            layero.find('.layui-layer-btn0').click();
                        }
                    });
                },
                end: function () {
                    $(document).off('keydown.enterSubmit');
                },
                yes: function (index, layero){
                    const btn = $(layero).find(".layui-layer-btn0");
                    const newPassword = $(layero).find("#newPassword").val();
                    const newPasswordRepeat = $(layero).find("#newPasswordRepeat").val();
                    const pwdRule = /^[\x00-\x7F]{5,}$/; // ASCII字符 0~127，长度≥5
                    if (!newPassword || !pwdRule.test(newPassword) || newPassword.length < 5) {
                        layer.msg("请输入5位以上的字母、数字、符号", {offset: '200px'});
                        return;
                    }
                    if (newPassword != newPasswordRepeat) {
                        layer.msg("两次密码输入不一致", {offset: '200px'});
                        return;
                    }
                    btn.text("正在提交...");
                    const load = layer.load(0);
                    $.ajax({
                        url: '../api/auth/changePassword',
                        method: 'post',
                        data: {newPassword}
                    }).then(result => {
                        const code = result.code;
                        const msg = result.msg;
                        if(code != 200){
                            layer.msg("操作失败: " + msg);
                            btn.text("提交");
                            layer.close(load);
                            return;
                        }
                        layer.closeAll();
                        layer.msg("修改密码成功");
                    }, () => {
                        layer.msg("操作失败");
                        btn.text("提交");
                        layer.close(load);
                    });
                }
            })
        });
        // 退出登录事件
        $(".layui-nav-avatar .logout").on("click", function () {
            localStorage.clear();
            indexedDB.deleteDatabase(DB_NAME);
            layer.msg("正在退出登录...", { time: 500 }, function () {
                layer.msg("已退出登录", { time: 1000 }, function () {
                    location.href = "../pages/liveChat.html";
                });
            });
        });
    });
}

function getColorFromUsername(username) {
    const colors = [
        "#1abc9c", "#3498db", "#9b59b6", // 绿松石 蓝 紫
        "#e67e22", "#e74c3c", "#2ecc71", "#f1c40f" // 橙 红 绿 黄
    ];
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

let db;

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
async function getEmoteByName(name) {
    if (!db) {
        return null; // 数据库未初始化
    }

    return new Promise((resolve, reject) => {
        const transaction = db.transaction(STORE_NAME, "readonly");
        const store = transaction.objectStore(STORE_NAME);
        const request = store.get(name);

        request.onsuccess = (event) => {
            resolve(event.target.result || null);
        };

        request.onerror = () => {
            reject(null);
        };
    });
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
    if (!auth_user) {
        // 登录
        showDialog("dialog/login.html", {
            title: "请先登录",
            shadeClose: false,
            shade: 0.5,
            btn: ["登录"],
            success: function (layero, index) {
                $(layero).find("#captchaImg").click(function () {
                    $.ajax({url: '../api/auth/captcha'}).then(result => {
                        if (result.code === 200) {
                            $(layero).find("#captchaImg").attr("src", result.msg);
                            $(layero).find(".captcha-area").removeClass("layui-hide");
                        }
                    });
                });
                $(document).on('keydown.enterSubmit', function(e) {
                    if (e.key === 'Enter') {
                        layero.find('.layui-layer-btn0').click();
                    }
                });
            },
            end: function () {
                $(document).off('keydown.enterSubmit');
            },
            yes: function (index, layero){
                const btn = $(layero).find(".layui-layer-btn0");
                const userName = $(layero).find("#userName").val();
                const password = $(layero).find("#password").val();
                const captcha = $(layero).find("#captcha").val();
                if (!userName || !password) {
                    layer.msg("请输入用户名和密码", {time: 1000});
                    return;
                }
                if ($(layero).find("#captchaImg").is(":visible") && !captcha) {
                    layer.msg("请输入验证码", {time: 1000});
                    return;
                }
                btn.text("正在登录...");
                const load = layer.load(0);
                $(layero).find(".errorInfo").addClass("layui-hide");
                $.ajax({
                    url: '../api/auth/login',
                    method: 'post',
                    data: {userName, password, captcha}
                }).then(result => {
                    const code = result.code;
                    const msg = result.msg;
                    const count = result.count;
                    if(code != 200){
                        layer.msg(msg, {time: 1000});
                        $(layero).find(".errorInfo").text(msg);
                        $(layero).find(".errorInfo").removeClass("layui-hide");
                        btn.text("登录");
                        layer.close(load);
                        if (count > 2) {
                            $(layero).find("#captchaImg").trigger("click");
                            $(layero).find("#captcha").val("");
                        }
                        return;
                    }
                    layer.closeAll();
                    localStorage.setItem('auth_token', msg);
                    localStorage.setItem('auth_user', userName);
                    layer.msg("登录成功");
                    location.href = "../pages/liveChat.html";
                }, () => {
                    btn.text("登录");
                    layer.close(load);
                    $(layero).find(".errorInfo").text("登录失败");
                    $(layero).find(".errorInfo").removeClass("layui-hide");
                });
            }
        })
        return;
    }
    $.ajaxSetup({
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("auth_token"),
            "X-Schema": layui.data("navInfo")["schema"],
        }
    });
    openDB().then(() => {
        // 获取数据字典
        let emoteDict = {};
        // 检查是否有STORAGE_OK
        let storageOkEmote = layui.data("emoteDict");
        if (!storageOkEmote || !storageOkEmote["STORAGE_OK"] || storageOkEmote["STORAGE_OK"] != "2") {
            // 如果没有STORAGE_OK标记，则进行ajax请求获取数据
            $.ajax({url: '../api/liveChat/queryEmotes'}).then(data => {
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

function getEmoteMessage(message) {
    if (!message) return Promise.resolve("");

    // 判断是 YouTube 风格还是 Twitch 风格
    const isYouTube = message.indexOf(":") > -1;

    if (isYouTube) {
        const parts = message.split(":");
        const first = parts.shift(); // 保留第一个
        const last = parts.pop();    // 保留最后一个
        const promises = parts.map(key =>
            getEmoteByName(key).then(emote => ({ key, emote }))
        );

        return Promise.all(promises).then(results => {
            let realMessage = first;
            for (const { key, emote } of results) {
                if (emote) {
                    realMessage += emote.isCustomEmoji ? `<img alt="" class="emote" src="${emote.images}">` : emote.emotesId;
                } else if (key.trim()) {
                    realMessage += `[${key}]`;
                }
            }
            // 添加尾部
            if (last) realMessage += last;
            return realMessage;
        });
    } else {
        // Twitch 风格
        const splits = message.split(" ");
        const promises = splits.map(split =>
            getEmoteByName(split).then(emote => ({ split, emote })).catch(() => ({ split, emote: null }))
        );

        return Promise.all(promises).then(results => {
            return results.map(({ split, emote }) => {
                if (emote) return `<img class="emote" src="${emote.images}">`;
                if (split.trim() && /^[a-zA-Z ]+$/.test(split)) return `[${split}]`;
                return ` ${split} `;
            }).join("");
        });
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
    if(!time){
        return title;
    }
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
    if (!auth_user) {
        return;
    }
    $.ajax({url: '../api/liveInfo/queryListBySelector'}).then((selectorList)=>{
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
    }, (res) => {
        if (res.status == 401) {
            localStorage.clear();
            indexedDB.deleteDatabase(DB_NAME);
            layer.msg("登录状态已过期，请重新登录", {time: 2500}, () => {
                location.href = "../pages/liveChat.html"
            });
            return;
        }
        layer.msg("系统出错: " + res.statusText);
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
// 支持拖动文件到文本框
function dragFile(textarea) {
    // 阻止默认行为
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        textarea.addEventListener(eventName, e => e.preventDefault());
    });
    textarea.addEventListener('dragover', () => textarea.classList.add('hover'));
    textarea.addEventListener('dragleave', () => textarea.classList.remove('hover'));
    textarea.addEventListener('drop', (e) => {
        textarea.classList.remove('hover');
        const file = e.dataTransfer.files[0];
        if (file && /\.(md|txt|json|csv|xml|html?)$/i.test(file.name)) {
            const reader = new FileReader();
            reader.onload = (event) => {
                textarea.value = event.target.result;
                layer.msg('复制成功');
            };
            reader.readAsText(file, 'utf-8');
        } else {
            layer.msg('不支持的文件类型，只支持文本类文档', {icon: 2});
        }
    });
}