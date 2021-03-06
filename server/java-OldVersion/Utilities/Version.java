/*******************************************************************************
 * ABOS
 * Copyright (C) 2018 Patrick Magauran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package Utilities;

public class Version {

    private final int major;
    private final int minor;
    private final int revision;

    public Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public Version(String version) {
        String[] versionArray = version.split("\\.");
        this.major = versionArray.length >= 1 ? Integer.valueOf(versionArray[0]) : 0;
        this.minor = versionArray.length >= 2 ? Integer.valueOf(versionArray[1]) : 0;
        this.revision = versionArray.length >= 3 ? Integer.valueOf(versionArray[2]) : 0;
    }

    public static String format(String version) {
        return new Version(version).toString();
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public boolean greaterThan(String version) {
        String[] versionArray = version.split("\\.");

        int majorVal = versionArray.length >= 1 ? Integer.valueOf(versionArray[0]) : 0;
        int minorVal = versionArray.length >= 2 ? Integer.valueOf(versionArray[1]) : 0;
        int revisionVal = versionArray.length >= 3 ? Integer.valueOf(versionArray[2]) : 0;
        return major > majorVal || (major >= majorVal && minor > minorVal) || (major >= majorVal && minor >= minorVal && revision > revisionVal);
    }

    public boolean greaterThanOrEqual(String version) {
        String[] versionArray = version.split("\\.");
        int majorVal = versionArray.length >= 1 ? Integer.valueOf(versionArray[0]) : 0;
        int minorVal = versionArray.length >= 2 ? Integer.valueOf(versionArray[1]) : 0;
        int revisionVal = versionArray.length >= 3 ? Integer.valueOf(versionArray[2]) : 0;
        return (major >= majorVal && minor >= minorVal && revision >= revisionVal) || (major > majorVal) || (major >= majorVal && minor > minorVal);
    }

    public boolean greaterThan(Version version) {

        int majorVal = version.major;
        int minorVal = version.minor;
        int revisionVal = version.revision;
        return major > majorVal || (major >= majorVal && minor > minorVal) || (major >= majorVal && minor >= minorVal && revision > revisionVal);
    }

    @Override
    public boolean equals(Object version) {
        if (version == null) return false;
        if (version == this) return true;
        if (!(version instanceof String) && !(version instanceof Version)) return false;
        if (version instanceof Version) {
            return (major == ((Version) version).major && minor == ((Version) version).minor && revision == ((Version) version).revision);

        } else {
            String[] versionArray = ((String) version).split("\\.");
            int majorVal = versionArray.length >= 1 ? Integer.valueOf(versionArray[0]) : 0;
            int minorVal = versionArray.length >= 2 ? Integer.valueOf(versionArray[1]) : 0;
            int revisionVal = versionArray.length >= 3 ? Integer.valueOf(versionArray[2]) : 0;
            return (major == majorVal && minor == minorVal && revision == revisionVal);

        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(major) + Integer.hashCode(minor) + Integer.hashCode(revision);
    }

    public boolean greaterThanOrEqual(Version version) {
        int majorVal = version.major;
        int minorVal = version.minor;
        int revisionVal = version.revision;
        return (major >= majorVal && minor >= minorVal && revision >= revisionVal) || (major > majorVal) || (major >= majorVal && minor > minorVal);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + revision;
    }
}
