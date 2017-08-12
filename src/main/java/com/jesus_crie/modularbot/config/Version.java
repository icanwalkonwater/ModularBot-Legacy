package com.jesus_crie.modularbot.config;

/**
 * Represent a version number
 */
public class Version {

    private final int major;
    private final int minor;
    private final int revision;
    private final int build;

    public Version(int major, int minor, int revision, int build) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
    }

    public static Version of(int major, int minor, int revision, int build) {
        return new Version(major, minor, revision, build);
    }

    /**
     * @return the major version.
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return the minor version.
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @return the revision number.
     */
    public int getRevision() {
        return revision;
    }

    /**
     * @return the build number.
     */
    public int getBuild() {
        return build;
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s_%s", major, minor, revision, build);
    }
}
