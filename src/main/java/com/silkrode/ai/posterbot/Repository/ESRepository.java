package com.silkrode.ai.posterbot.Repository;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Repository
public class ESRepository implements ESRepositoryInterface {

    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("10.205.48.52", 9200)));

    @Override
    public String getNewsTopic(String index, String category, String country) throws IOException {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = df.format(time);
        LocalDate localDate = LocalDate.parse(timeStr.split(" ")[0]);
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest(index));
        request.setScriptType(ScriptType.INLINE);
        request.setScript(
                "{\n" +
                        "  \"query\": {\n" +
                        "   \n" +
                        "    \"bool\": {\n" +
                        "      \"must\": [\n" +
                        "        {\n" +
                        "          \"match\": {\n" +
                        "            \"date\": \"" + localDate.plusDays(-1) + "\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        {\n" +
                        "         \"match\": {\n" +
                        "            \"category.keyword\": \"" + category + "\"\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  },   \n" +
                        "  \"size\": 1,\n" +
                        "  \"sort\": [\n" +
                        "    {\n" +
                        "      \"news_count\": {\n" +
                        "        \"order\": \"desc\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"_source\": [\"topic\",\"lead_source\"]" +
                        "}"
        );
        Map<String, Object> params = new HashMap<>();
        params.put("value", "elasticsearch");
        request.setScriptParams(params);
        SearchTemplateResponse response = client.searchTemplate(request, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getResponse().getHits();
        String result = "";
        String id = searchHits.getAt(0).getId();
        result += searchHits.getAt(0).getSourceAsMap().get("topic");
        String url = "";
            if(country.equals("tw")) {
            String s = searchHits.getAt(0).getSourceAsMap().get("lead_source").toString();
            url = s.substring(s.indexOf("=")+1,s.length()-1);
        }
        else
            url = "https://utopian-datum-247606.df.r.appspot.com/"+id;
        result += "\n" + url;
        return result;
    }


}
