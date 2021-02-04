package com.silkrode.ai.posterbot.service;


import com.silkrode.ai.posterbot.News;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface PosterBotServiceInterface {

    Object postNews(News news) throws IOException, JSONException;

    Object reply() throws IOException;

    Object comment() throws IOException;
}
