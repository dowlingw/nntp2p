package io.phy.nntp2p.protocol;

/**
 * Because org.apache.commons.net.nntp.NNTPCommand is just a bit shit
 */
public enum NNTPCommand {

//    BODY,
//    GROUP,
//    HEAD,
//    HELP,
//    IHAVE,
//    LAST,
//    LIST,
//    NEWGROUPS,
//    NEWNEWS,
//    NEXT,
//    POST,
//    QUIT,
//    SLAVE,
//    STAT,
//    AUTHINFO,
//    XOVER,
//    XHDR,


    ARTICLE,

    // RFC3977 extensions provided by NNTP2P
    PEER,   // Identify as a peer
}
