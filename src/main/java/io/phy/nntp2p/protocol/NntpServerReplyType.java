package io.phy.nntp2p.protocol;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum NntpServerReplyType {
    // DO NOT REMOVE ITEMS FROM THIS LIST
    SERVER_READY_POSTING_ALLOWED (200),
    SERVER_READY_POSTING_NOT_ALLOWED (201),
    CLOSING_CONNECTION (205),
    ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW (220),
    ARTICLE_RETRIEVED_BODY_FOLLOWS (222),
    ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY (223),
    AUTHENTICATION_ACCEPTED (281),
    PASSWORD_REQUIRED(381),
    SERVICE_TEMPORARILY_UNAVAILABLE (400),
    NO_SUCH_ARTICLE_FOUND (430),
    AUTHENTICATION_REQUIRED (480),
    AUTH_REJECTED (481),
    AUTH_OUT_OF_SEQUENCE (482),
    COMMAND_NOT_RECOGNIZED (500),
    COMMAND_SYNTAX_ERROR (501),
    COMMAND_UNAVAILABLE(502);
    ;

    // Allow reverse lookups
    private static final Map<Integer,NntpServerReplyType> lookup = new HashMap<Integer,NntpServerReplyType>();
    static {
        for(NntpServerReplyType s : EnumSet.allOf(NntpServerReplyType.class))
            lookup.put(s.responseCode, s);
    }

    // Reverse Lookups
    public static NntpServerReplyType Resolve(Integer responseCode) {
        return lookup.get(responseCode);
    }

    protected Integer responseCode;

    NntpServerReplyType(Integer responseCode) {
     this.responseCode = responseCode;
    }

    public boolean isInformational()
    {
        return (responseCode >= 100 && responseCode < 200);
    }

    public boolean isPositiveCompletion()
    {
        return (responseCode >= 200 && responseCode < 300);
    }

    public boolean isPositiveIntermediate()
    {
        return (responseCode >= 300 && responseCode < 400);
    }

    public boolean isNegativeTransient()
    {
        return (responseCode >= 400 && responseCode < 500);
    }

    public boolean isNegativePermanent()
    {
        return (responseCode >= 500 && responseCode < 600);
    }
}