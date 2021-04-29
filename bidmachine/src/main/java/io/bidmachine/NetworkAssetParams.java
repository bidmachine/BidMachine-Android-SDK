package io.bidmachine;

import androidx.annotation.NonNull;

class NetworkAssetParams {

    private final String name;
    private final String version;
    private final String classpath;

    public NetworkAssetParams(@NonNull String name,
                              @NonNull String version,
                              @NonNull String classpath) {
        this.name = name;
        this.version = version;
        this.classpath = classpath;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getClasspath() {
        return classpath;
    }

}