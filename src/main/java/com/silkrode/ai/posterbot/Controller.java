package com.silkrode.ai.posterbot;

import com.silkrode.ai.posterbot.service.PostBotService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/posterbot")
public class Controller {


    @Autowired
    private PostBotService postBotService;

    @PostMapping("/post")
    public ResponseEntity postNews(@RequestBody News news) throws IOException {
        return ResponseEntity.ok().body(postBotService.postNews(news));
    }

    @GetMapping("/reply")
    public Object reply() throws IOException {
        return postBotService.reply();
    }

    @GetMapping("/comment")
    public Object comment() throws IOException {
        return postBotService.comment();
    }

}
