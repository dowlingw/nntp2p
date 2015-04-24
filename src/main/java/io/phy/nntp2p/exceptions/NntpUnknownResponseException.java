package io.phy.nntp2p.exceptions;

public class NntpUnknownResponseException extends Exception  {
    private String response;
    public NntpUnknownResponseException(String s) {
        response = s;
    }

    public String getResponse() {
        return response;
    }
}
