package io.phy.nntp2p.protocol;

import java.util.ArrayList;
import java.util.List;

public class NntpServerReply {
    private NntpServerReplyType responseCode;
    private List<String> sections;

    public NntpServerReply(NntpServerReplyType command) {
        this.responseCode = command;
        sections = new ArrayList<>();
    }

    public void addArg(String arg) {
        sections.add(arg);
    }

    public void addArg(Integer arg) {
        sections.add(arg.toString());
    }

    public NntpServerReplyType getResponseCode() {
        return responseCode;
    }

    public List<String> getSections() {
        return sections;
    }

}
