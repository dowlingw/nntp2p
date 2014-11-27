package io.phy.nntp2p.protocol;

/**
 * Because org.apache.commons.net.nntp.NNTPCommand is just a bit shit
 */
public enum NNTPCommand {

//    GROUP,
//    HELP,
//    IHAVE,
//    LAST,
//    LIST,
//    NEWGROUPS,
//    NEWNEWS,
//    NEXT,
//    POST,
    QUIT,
//    SLAVE,
    AUTHINFO,
//    XOVER,
//    XHDR,

    HEAD,
    STAT,
    BODY,
    ARTICLE,
}
