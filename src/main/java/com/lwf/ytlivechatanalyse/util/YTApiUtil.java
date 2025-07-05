package com.lwf.ytlivechatanalyse.util;

import com.alibaba.fastjson.JSONObject;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.util.*;


public class YTApiUtil {

    public static void main(String[] args) throws Exception {
        // 设置 SOCKS5 代理
//        System.setProperty("socksProxyHost", "127.0.0.1");
//        System.setProperty("socksProxyPort", "7890");

        String apiKey = "apiKey";

        List<String> ids = Arrays.asList("_I0PpMs_NTk", "oQ5NK0_mWm8");
        List<String> parts = Arrays.asList("status", "contentDetails");

        Map<String, JSONObject> infoMap = YTApiUtil.getVideoInfo(apiKey, ids, parts);

        for (Map.Entry<String, JSONObject> entry : infoMap.entrySet()) {
            System.out.println("视频ID: " + entry.getKey());
            System.out.println("信息: " + entry.getValue().toJSONString());
        }

        List<JSONObject> comments = YTApiUtil.getVideoComments(apiKey, "oQ5NK0_mWm8", 10, "relevance");

        for (JSONObject comment : comments) {
            System.out.println("作者: " + comment.getString("author"));
            System.out.println("内容: " + comment.getString("text"));
            System.out.println("👍: " + comment.getIntValue("likeCount"));
            System.out.println("时间: " + comment.getString("publishedAt"));
            System.out.println("----");
        }
    }

    private static final String APPLICATION_NAME = "YTApiUtil";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * 获取多个视频的指定信息。
     *
     * @param apiKey     YouTube Data API v3 的 API Key
     * @param videoIds   视频 ID 列表
     * @param partFields 要获取的字段（如 status, contentDetails, snippet 等）
     * @return Map：视频ID -> 对应字段信息（Fastjson 格式）
     * @throws Exception
     */
    public static Map<String, JSONObject> getVideoInfo(String apiKey, List<String> videoIds, List<String> partFields) throws Exception {
        YouTube youtubeService = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> {
                }
        ).setApplicationName(APPLICATION_NAME).build();

        String ids = String.join(",", videoIds);
        String parts = String.join(",", partFields);

        YouTube.Videos.List request = youtubeService.videos()
                .list(parts)
                .setId(ids)
                .setKey(apiKey);

        VideoListResponse response = request.execute();

        Map<String, JSONObject> resultMap = new HashMap<>();

        for (Video video : response.getItems()) {
            JSONObject json = new JSONObject();

            if (partFields.contains("status")) {
                json.put("status", JSONObject.parseObject(video.getStatus().toString()));
            }

            if (partFields.contains("snippet")) {
                json.put("snippet", JSONObject.parseObject(video.getSnippet().toString()));
            }

            if (partFields.contains("statistics")) {
                json.put("statistics", JSONObject.parseObject(video.getStatistics().toString()));
            }

            if (partFields.contains("contentDetails")) {
                json.put("contentDetails", JSONObject.parseObject(video.getContentDetails().toString()));
            }

            if (partFields.contains("player")) {
                json.put("player", JSONObject.parseObject(video.getPlayer().toString()));
            }

            if (partFields.contains("topicDetails") && video.getTopicDetails() != null) {
                json.put("topicDetails", JSONObject.parseObject(video.getTopicDetails().toString()));
            }

            if (partFields.contains("recordingDetails") && video.getRecordingDetails() != null) {
                json.put("recordingDetails", JSONObject.parseObject(video.getRecordingDetails().toString()));
            }

            if (partFields.contains("liveStreamingDetails") && video.getLiveStreamingDetails() != null) {
                json.put("liveStreamingDetails", JSONObject.parseObject(video.getLiveStreamingDetails().toString()));
            }

            resultMap.put(video.getId(), json);
        }

        return resultMap;
    }

    public static List<JSONObject> getVideoComments(String apiKey, String videoId, int maxResults, String order) throws Exception {
        YouTube youtubeService = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                request -> {}
        ).setApplicationName("YTCommentUtil").build();

        YouTube.CommentThreads.List request = youtubeService.commentThreads()
                .list("snippet")
                .setVideoId(videoId)
                .setMaxResults((long) maxResults)
                .setOrder(order)
                .setTextFormat("plainText")
                .setKey(apiKey);

        CommentThreadListResponse response = request.execute();

        List<JSONObject> comments = new ArrayList<>();

        for (CommentThread thread : response.getItems()) {
            CommentSnippet topComment = thread.getSnippet().getTopLevelComment().getSnippet();

            JSONObject obj = new JSONObject();
            obj.put("author", topComment.getAuthorDisplayName());
            obj.put("text", topComment.getTextDisplay());
            obj.put("likeCount", topComment.getLikeCount());
            obj.put("publishedAt", topComment.getPublishedAt().toStringRfc3339());

            comments.add(obj);
        }

        return comments;
    }
}
