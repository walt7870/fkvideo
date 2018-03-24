// var flashvars = {
//     f: './fkvideo/video/11.flv',    //视频地址
//     c: 0,            //调用ckplayer.js中的ckstyle()
//     p: 2,            //默认不加载视频，点击播放才开始加载
//     i: './images/ntech.jpg'     //封面图地址
// };
// var params = {bgcolor: '#FFF', allowFullScreen: true, allowScriptAccess: 'always', wmode: 'transparent'};
// //设置播放器地址、视频容器id、宽高等信息
// CKobject.embedSWF('./ckplayer/ckplayer.swf', 'video1', 'ckplayer_a1', '450', '360', flashvars, params);
//playVideo("a",2)
var picBase64
$(document).ready(function () {

    fkstatus = 0;
    initTime = 8;
    myId = guid();
    port = 8080;
    var websocket;
    var repeatCon = 18;

    function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    function guid() {
        return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
    }

    function connectWs() {
        websocket = new WebSocket("ws://" + window.location.hostname + ":" + port + "/fkvideo/fkFacePushWs");
        websocket.onopen = openWs;
        websocket.onmessage = messageWs;
        websocket.onclose = closeWs;
        websocket.onerror = errorWs;

    }

    // if ('WebSocket' in window) {
    //     websocket = new WebSocket("ws://"+window.location.hostname+":"+port+"/fkvideo/fkFacePushWs");
    // } else if ('MozWebSocket' in window) {
    //     websocket = new MozWebSocket("ws://"+window.location.hostname+":"+port+"/fkvideo/fkFacePushWs");
    // } else {
    //     websocket = new SockJS("http://localhost:"+port+"/fkvideo/sockjs/fkFacePushWs");
    // }

    function openWs() {
        repeatCon = 18
        console.log("WebSocket连接成功");
        websocket.send(myId);
    }

    function messageWs(evnt) {
        if (evnt.data == "end") {
            fkstatus = 0;
            alert("视频检测结束");
            return false;
        }
        if (evnt.data == "stop") {
            return false;
        }
        if (evnt.data == "errorEnd") {
            console.log("errorEnd");
            return false;
        }
        getResult(evnt.data);
    }

    function closeWs() {
        console.log("WebSocket连接关闭");
        if (repeatCon > 0) {
            connectWs();
        }
        repeatCon = repeatCon - 1
    }

    function errorWs() {
        console.log("WebSocket连接出错");
    }
    //连接websocket
    connectWs();
    // websocket.onopen = function (evnt) {
    //     console.log("WebSocket连接成功");
    //     websocket.send(myId);
    //     // $("#msgcount").append("WebSocket链接开始！<br/>");
    // };
    // websocket.onmessage = function (evnt) {
    //     if(evnt.data=="end"){
    //         fkstatus = 0;
    //         alert("视频检测结束");
    //         return false;
    //     }
    //     if(evnt.data =="stop"){
    //         return false;
    //     }
    //     if(evnt.data=="errorEnd"){
    //         console.log("errorEnd");
    //         return false;
    //     }
    //     getResult(evnt.data);
    // };
    // websocket.onerror = function (evnt) {
    //     console.log("WebSocket连接出错");
    //     //$("#msgcount").append("WebSocket链接出错！<br/>");
    // };
    // websocket.onclose = function (evnt) {
    //     alert("websocket连接断开！请按ctrl+F5刷新网页");
    //     console.log("WebSocket连接关闭");
    //     //$("#msgcount").append("WebSocket链接关闭！<br/>");
    // };


    //GrindPlayer===============================
    serverIP = "rtmp://" + window.location.hostname + ":1935/livecam";
    var flashvars = {
        src: serverIP
        , streamType: "live"
        , scaleMode: "letterbox"
        , bufferTime: 3
    };
    var params = {
        allowFullScreen: true
        , allowScriptAccess: "always"
        , bgcolor: "#000000"
    };
    var attrs = {
        name: "player"
    };
    swfobject.embedSWF("static/grindPlayer/GrindPlayer.swf", "player", "42%", "98%", "10.2", null, flashvars, params, attrs);


    //==========================================
    $("#beginIdentify").click(function () {
        startFkvideoDetector();
    })

    $("#endIdentify").click(function () {
        stopIdentify();
    })
    $("input[name='fileType']").click(function () {
        var radioVal = $("input[name='fileType']:checked").val();
        if (radioVal == 'file') {
            $("#streamInput").hide();
            $("#videoList").show();
        }
        if (radioVal == 'stream') {
            $("#videoList").hide();
            $("#streamInput").show();
        }
    });

    function stopIdentify() {
        console.log("停止视频检测");
        if (fkstatus == 0) {
            alert("中止有误！");
            return false;
        }
        $.ajax({
            url: "/fkvideo/stopIdentify?myId=" + myId,
            type: "GET",
            dataType: null,
            success: function (data) {
                console.log(data);
                if (data == "stop failed") {
                    alert("中止有误！");
                }
                if (data == "stopped") {
                    fkstatus = 0;
                    alert("检测中止");
                }
                // $("#beginIdentify").attr("disabled", false);
                //playVideo("aa", 2)
                $("#resultShowTip").html("");
            },
            processData: false,
            contentType: false
        });
    }

    function startFkvideoDetector() {
        if (!$("input[name='photo']").val()) {
            alert("请选择一个一个图片");
            return false;
        }
        var threshold = $("input[name='threshold']").val()
        if (!/^0+(\.\d{1,3})?$/.test(threshold) || threshold <= 0 || threshold > 1) {
            alert("阈值需要在0到1之间");
            return false;
        }
        var radioVal = $("input[name='fileType']:checked").val();
        var videoName;
        if (radioVal == 'file') {
            if (!$("input[name='video']:checked").val()) {
                alert("请选择一个视频");
                return false;
            }
            videoName = $("input[name='video']:checked").val()
        } else if (radioVal == 'stream') {
            var streamVideo = $("input[name='fileStream']").val();
            if (!streamVideo || streamVideo.indexOf("rtsp") == -1) {
                alert("视频流需要是rtsp格式");
                return false;
            }
            videoName = streamVideo;
        } else {
            alert("视频格式不正确");
        }
        if (fkstatus == 1) {
            alert("视频检测已启动！");
            return false;
        }
        //初始化视频检测=========
        swfobject.embedSWF("static/grindPlayer/GrindPlayer.swf", "player", "42%", "98%", "10.2", null, flashvars, params, attrs);


        //================================
        var reader = new FileReader();
        var file = document.getElementById("photo").files[0];
        reader.onloadend = function (e) {
            picBase64 = e.target.result;
            // $("#preImage").html("<img src='"+picBase64+"'>");
            $("#preImage").attr("src", picBase64)
            $("#resultShowTip").html("<B>正在进行视频检测...</B>")
            $("#resultShow").html("")
            console.log(videoName)
            var videoDetect = new FormData();
            videoDetect.append("videoName", videoName);
            videoDetect.append("threshold", threshold);
            videoDetect.append("galleries", "test");
            videoDetect.append("fileType", radioVal);
            videoDetect.append("myId", myId);
            var fileObj = file;
            console.log(fileObj.name);
            videoDetect.append("img", fileObj); // 入人脸库的图片
            console.log("准备启动fkvideo_detector");
            $("#beginIdentify").attr("disabled", true);


            fkstatus = 1;
            $.ajax({
                url: "/fkvideo/detector/v0/fkdetector",
                type: "POST",
                data: videoDetect,
                dataType: "text",
                success: function (data) {
                    $("#resultShowTip").html("");
                    if (data == "errorStart") {
                        fkstatus = 0;
                        alert("启动fkvideo_detector失败");
                        console.log("启动fkvideo_detector失败");
                        return false;
                    }
                },
                error: function () {
                    $("#resultShowTip").html("");
                    console.log("error after detector");
                    // clearInterval(timer);
                    document.getElementById("player").stop2();
                    stopIdentify();
                },
                processData: false,
                contentType: false
            });
            //getResult()
        }
        reader.readAsDataURL(file);
    }

    var dataVideo = [];
    var xhr;
    // var timer;
    var player;
    getVideo();


    $("#getVideosBtn").click(function () {
        getVideo();
    })


    $("#addVideo").click(function () {
        var fileObj = document.getElementById("file").files[0];
        var url = "/fkvideo/upload"; // 接收上传文件的后台地址
        uploadFile(fileObj, url);
    })
    $("#cancelUpload").click(function () {
        cancleUploadFile();
    })

    function getResult(data) {
        // console.log(data);

        data = JSON.parse(data);
        // console.log(data['begin']);
        //$("#resultShow").html("");
        var demo = document.getElementById("resultShow").innerHTML
        if (data['faces']) {
            var beginTime = parseInt(data['begin']);
            var faces = data['faces']
            for (var i = 0; i < faces.length; i++) {
                var tmprResult = faces[i]['results'];
                var tmpTime = faces[i]['timestamp'];
                var catchFace = faces[i]['catchFace'];
                var catchView = faces[i]['catchView'];
                for (box in tmprResult) {
                    var boxFaces = tmprResult[box];
                    if (boxFaces.length == 0) {
                        continue;
                    } else {

                        var confidence = boxFaces['confidence'];
                        var verified = boxFaces['verified'];

                        //                        timePic=tmprResult[j]['photo'].split("/")[6].split(".")[0].substring(0,13);
                        //                            timePic = tmpTime.substring(45, 58)
                        second = (tmpTime - beginTime) / 1000
                        console.log("未处理视频时间：" + second)
                        second = second + initTime;


                        console.log(second);

                        demo = '<div class="max_box">' +
                            '<div class="img_box">' +
                            '<div class="img_box_min"> ' +
                            '<div class="img_border"><img src="' + catchFace + '"/></div>' +
                            '<div class="img_text">人脸截图</div>' +
                            '</div>' +
                            '<div class="img_box_min"> ' +
                            '<div class="img_border"><img src="' + catchView + '"/></div>' +
                            '<div class="img_text">场景截图</div>' +
                            '</div>' +
                            '</div>' +
                            '<div class="bottom_text">视频中出现时间：' + Math.round(second) + '(s)</div>' +
                            '<div class="bottom_text">可信度：' + confidence + '</div>' +
                            '</div>' + demo

                    }
                }

            }
        }
        $("#resultShow").html(demo)

    }
})

var ot;//
//上传文件方法
function getVideo() {
    $.ajax({
        url: "/fkvideo/result",
        type: "POST",
        dataType: "text",
        success: function (data) {
            $("#streamInput").hide();
            $("#videoList").show();
            console.log(data);
            dataVideo = data.split(",");
            console.log("dataVideo.length:" + dataVideo.length);
            var content = ""
            console.log("视频个数： " + dataVideo.length);
            for (var i = 0; i < dataVideo.length; i++) {
                var v = decodeURI(dataVideo[i]);
                if (dataVideo[i] == null || dataVideo == "")
                    continue;
                content += "<tr>" +
                    "<td><input type='radio' name='video' value='" + v + "' ></td>" +
                    "<td>" + v + "</td></tr>"
            }
            var restult = "<table>" + content + "</table>"
            $("#videoList").html(restult)
        },
        processData: false,
        contentType: false
    });

}

function uploadFile(fileObj, url) {
    //var fileObj = document.getElementById("file").files[0]; // js 获取文件对象
    if (!fileObj) {
        alert("请选择上传的视频");
        return false;
    }
    // var url = "/fkvideo/upload/"; // 接收上传文件的后台地址

    var form = new FormData(); // FormData 对象
    form.append("mf", fileObj); // 文件对象
    xhr = new XMLHttpRequest();  // XMLHttpRequest 对象
    xhr.open("post", url, true); //post方式，url为服务器请求地址，true 该参数规定请求是否异步处理。
    xhr.onload = function () {
        alert("上传成功")
        getVideo();
    };
    ; //请求完成
    xhr.onerror = function () {
        alert(" 上传失败")
    }; //请求失败
    xhr.upload.onprogress = progressFunction;//【上传进度调用方法实现】
    xhr.upload.onloadstart = function () {//上传开始执行方法
        ot = new Date().getTime();   //设置上传开始时间
        oloaded = 0;//设置上传开始时，以上传的文件大小为0
    };
    xhr.send(form); //开始上传，发送form数据
}

//上传进度实现方法，上传过程中会频繁调用该方法
function progressFunction(evt) {

    var progressBar = document.getElementById("progressBar");
    var percentageDiv = document.getElementById("percentage");
    // event.total是需要传输的总字节，event.loaded是已经传输的字节。如果event.lengthComputable不为真，则event.total等于0
    if (evt.lengthComputable) {//
        progressBar.max = evt.total;
        progressBar.value = evt.loaded;
        percentageDiv.innerHTML = Math.round(evt.loaded / evt.total * 100) + "%";
    }

    var time = document.getElementById("time");
    var nt = new Date().getTime();//获取当前时间
    var pertime = (nt - ot) / 1000; //计算出上次调用该方法时到现在的时间差，单位为s
    ot = new Date().getTime(); //重新赋值时间，用于下次计算

    var perload = evt.loaded - oloaded; //计算该分段上传的文件大小，单位b
    oloaded = evt.loaded;//重新赋值已上传文件大小，用以下次计算

    //上传速度计算 单位b/s
    var speed = perload / pertime;
    var bspeed = speed;
    var units = 'b/s';//单位名称
    if (speed / 1024 > 1) {
        speed = speed / 1024;
        units = 'k/s';
    }
    if (speed / 1024 > 1) {
        speed = speed / 1024;
        units = 'M/s';
    }
    speed = speed.toFixed(1);
    //剩余时间
    var resttime = ((evt.total - evt.loaded) / bspeed).toFixed(1);
    time.innerHTML = '，速度：' + speed + units + '，剩余时间：' + resttime + 's';
    if (bspeed == 0)
        time.innerHTML = '上传已取消';
}

function playVideo(url, auto) {
    var flashvars = {
        f: url,    //视频地址
        c: 0,            //调用ckplayer.js中的ckstyle()
        p: auto,            //默认不加载视频，点击播放才开始加载
        i: '.static/img/max_logo.png'     //封面图地址
    };
    var params = {bgcolor: '#FFF', allowFullScreen: true, allowScriptAccess: 'always', wmode: 'transparent'};//设置播放器地址、视频容器id、宽高等信息
    CKobject.embedSWF('static/ckplayer/ckplayer.swf', 'video1', 'ckplayer_a1', '100%', '100%', flashvars, params)

}

//上传成功响应
function uploadComplete(evt) {
    //服务断接收完文件返回的结果
    //    alert(evt.target.responseText);
    alert("上传成功！");
}

//上传失败
function uploadFailed(evt) {
    alert("上传失败！");
}

//取消上传
function cancleUploadFile() {
    xhr.abort();
}





