package io.phy.nntp2p.configuration;

/**
 * Connection type enumeration
 * Order in this enumeration will determine cache priority level
 */
public enum ConnectionType {
    LocalCache,
    RemoteCache,
    Primary,
    Backup
}
