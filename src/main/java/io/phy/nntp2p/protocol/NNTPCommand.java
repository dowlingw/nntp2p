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
//    QUIT,
//    SLAVE,
//    AUTHINFO,
//    XOVER,
//    XHDR,

    HEAD,
    STAT,
    BODY,
    ARTICLE,

    // TODO: Remove this command by implementing Authentication
    // RFC3977 extensions provided by NNTP2P
    PEER,   // Identify as a peer
}
