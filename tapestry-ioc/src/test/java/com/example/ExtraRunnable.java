package com.example;

// TAP5-2130
public interface ExtraRunnable extends Runnable
{
    // I suppose this is plausable:
    void runOrThrow() throws Exception;
}
