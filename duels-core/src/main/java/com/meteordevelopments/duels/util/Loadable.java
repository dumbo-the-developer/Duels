package com.meteordevelopments.duels.util;

public interface Loadable {

    void handleLoad() throws Exception;

    void handleUnload() throws Exception;
}
