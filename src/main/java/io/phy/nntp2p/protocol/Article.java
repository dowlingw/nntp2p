package io.phy.nntp2p.protocol;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private Multimap<String,String> headers;
    private List<String> contents;

    public Article() {
        headers = ArrayListMultimap.create();
        contents = new ArrayList<>();
    }

    public Multimap<String, String> getHeaders() {
        return headers;
    }

    public List<String> getContents() {
        return contents;
    }
}
