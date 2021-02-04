package com.silkrode.ai.posterbot.service;

import com.silkrode.ai.posterbot.News;
import com.silkrode.ai.posterbot.Repository.ESRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

@Service
public class PostBotService implements PosterBotServiceInterface {

    private ESRepository esRepository = new ESRepository();
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public Object postNews(News news) throws IOException {

        StringBuilder index = new StringBuilder();
        index.append(news.getCountry().equals("tw") ? "daily_issues-news-tw_issue" : "daily_issues-news-cn_issue");
        String topic = esRepository.getNewsTopic(index.toString(), news.getCategory(),news.getCountry());
        topic = news.getCategory() + "\n" + topic;
        String tweetURL = "https://analysis.65lzg.com/api/social_media/twitter/tweet";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("tweetContent", topic);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(tweetURL, request, String.class);
        String tweetIDAndContent = result.getBody();
        File file = new File("TwitterPostID.txt");
        if (!file.exists())
            file.createNewFile();
        FileReader fileReader = new FileReader("TwitterPostID.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> idList = new ArrayList<>();
        while (bufferedReader.ready()) {
            idList.add(bufferedReader.readLine());
        }
        bufferedReader.close();
        if (idList.size() == 6) {
            FileWriter fileWriter = new FileWriter("TwitterPostID.txt", false);
            for (int i = 1; i < 6; i++) {
                fileWriter.write(idList.get(i) + "\n");
            }
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter("TwitterPostID.txt", true);
        fileWriter.write(tweetIDAndContent + "\n");
        fileWriter.close();
        return result;
    }

    @Override
    public Object comment() throws IOException {
        FileReader fileReader = new FileReader("TwitterPostID.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String commentURL = "https://analysis.65lzg.com/api/social_media/twitter/autoComment";
        while (bufferedReader.ready()) {
            String[] tweetDetail = bufferedReader.readLine().split("&&");
            String status_id = tweetDetail[0];
            Map comment = getComment(tweetDetail[1], "positive");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("id", status_id);
            map.put("comment", comment.get("_result"));
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
            restTemplate.postForEntity(commentURL, request, String.class);
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public Object reply() throws IOException {
        File file = new File("reply.txt");
        if (!file.exists())
            file.createNewFile();
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> replyId = new ArrayList<>();
        while (bufferedReader.ready())
            replyId.add(bufferedReader.readLine());
        bufferedReader.close();
        String commentURL = "https://analysis.65lzg.com/api/social_media/twitter/commentReply";
        List<String> commentList = getreply();
        for (String s : commentList) {
            String[] comment = s.split("&&");
            String status_id = comment[1];
            if (!replyId.contains(status_id)) {
                String reply = getComment(comment[0], "positive").get("_result").toString();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                Map<String, Object> map = new HashMap<>();
                map.put("id", status_id);
                map.put("comment", reply);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
                restTemplate.postForEntity(commentURL, request, String.class);
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(status_id + "\n");
                fileWriter.close();
            }
        }
        return ResponseEntity.ok().build();
    }

    public Map getComment(String title, String sentiment) {
        String taiwanCommentURL = "https://analysis.65lzg.com/api/taiwan/comment";
        String chinaCommentURL = "https://analysis.65lzg.com/api/china/comment";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map map = new HashMap<>();
        map.put("title", title);
        map.put("mode", "news");
        map.put("sentiment", sentiment);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> resultComment = restTemplate.postForEntity(chinaCommentURL, request, Map.class);
        return resultComment.getBody();
    }

    public List<String> getreply() {
        String tweetURL = "https://analysis.65lzg.com/api/social_media/twitter/getReply";
        List<String> comment = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<List> result = restTemplate.postForEntity(tweetURL, request, List.class);
        comment.addAll(result.getBody());
        return comment;
    }

}
