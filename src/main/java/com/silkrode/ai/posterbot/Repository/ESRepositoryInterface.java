package com.silkrode.ai.posterbot.Repository;

import java.io.IOException;

public interface ESRepositoryInterface {

    Object getNewsTopic(String inedx, String category, String country) throws IOException;
}
