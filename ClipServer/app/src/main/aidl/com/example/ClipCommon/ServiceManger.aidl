package com.example.ClipCommon;

interface ServiceManger {
    int test();
    String[] getSongList();
    void playSongAtId(int i);
    void pause();
    void resume();
    void stop();
    void closeService();
}
