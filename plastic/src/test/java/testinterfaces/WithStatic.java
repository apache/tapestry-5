package testinterfaces;

public interface WithStatic extends Runnable {
    static int version() {
        return 1;
    }
}
