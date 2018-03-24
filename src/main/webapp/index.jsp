<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
	<head>
		<meta charset="utf-8" />
		<title>视频检测</title>
		<script src="static/js/jQuery-3.2.1.src.js"></script>
		<script type="text/javascript" src="static/grindPlayer/grindPlayer.js"></script>
		<script type="text/javascript" src="static/js/index.js" ></script>
		<link rel="stylesheet" href="static/css/index.css" />
	</head>
	<body>
	<div class="main">
		<div class="left_box">
			<div class="left_logo"></div>
			<div class="left_video">
				<div class="video_bg">
					<div class="left_title">
						<div class="title_text">上传视频文件</div>
						<div class="title_choose">
							<div class="choose_text">选择视频：</div>
							<div class="choose_browse"><input id="file" type="file" class="choose_pic"></div>
						</div>
						<div class="title_upload">
							<div class="choose_progress">
								<div class="progress_text">上传进度:</div>
								<div class="progress_results">
									<progress max="100" value="0" id="progressBar"></progress>
									<span id="percentage"></span><span id="time"></span>
								</div>
							</div>
						</div>
						<div class="title_btn">
							<div class="btn_upload" id="addVideo">上传</div>
							<div class="btn_candel" id="cancelUpload">取消</div>
						</div>
					</div>
					<div class="right_title">
						<div style="float: left">
							<input style="float: left;display: block" type="radio" name="fileType" value="file" checked><div class="title_text">视频文件</div>
						</div>
						<div style="float: left">
							<input style="float: left;display: block" type="radio" name="fileType" value="stream"><div class="title_text">视频流</div>
						</div>

						<div class="title_vedio" id="videoList">
								<%--视频列表--%>
						</div>
						<div id="streamInput">
							<div class="title_text">视频流地址</div><input type="text" name="fileStream">
						</div>
					</div>
				</div>
			</div>
			<div class="left_photo">
				<div class="photo_bg">
					<div class="bg_title">
						<div class="left_title2">
							<div class="title_text">选择需要辨认的人物照片</div>
							<div class="title_choose2">
								<div class="choose_text">选择图片：</div>
								<div class="choose_browse"><input id="photo" name="photo" type="file" class="choose_pic"></div>
							</div>
							<div class="title_upload2">
								<div class="choose_progress">
									<div class="progress_text">设置阈值:</div>
									<div class="progress_results">
										<div class="select_box">
											<div class="slippage"><input id="threshold" name="threshold" type="number" min="0" max="1" value="0.68" step="0.02"/></div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="right_photo">
							<div class="photo_img">
								<img id="preImage" src="static/img/preImage.jpeg"/>
							</div>
						</div>
					</div>
				</div>
				<div class="photo_btn">
					<div class="btn_top btn_start" id="beginIdentify"></div>
					<div class="btn_top btn_stop" id="endIdentify"></div>
				</div>
			</div>
		</div>
		<div class="right_box">
			<div class="border_box">
				<div class="box_video" id="player">
					<embed src=static/grindPlayer/GrindPlayer.swf width="100%" height="50%" type=application/x-shockwave-flash
						   wmode="transparent" quality="high" ;></embed>
				</div>
				<div class="box_results">
					<div class="results_title">
						<span>搜索结果</span>
					</div>
					<div class="prompting" id="resultShowTip">正在搜索...</div>
					<div class="box_content" id="resultShow">
						<%--结果展示--%>
					</div>
				</div>
			</div>
		</div>
	</div>
	</body>


</html>