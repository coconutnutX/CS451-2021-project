package cs451;

public class Constants {
    public static final int ARG_LIMIT_CONFIG = 7;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for output
    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    // indexes for config
    public static final int CONFIG_VALUE = 6;

    // resend check period (ms)
    public static final int RESEND_PERIOD = 300;

    // urb buffered send check period (ms)
    public static final int INIT_URB_BUFFERED_SEND_PERIOD = 500;
    public static final int MAX_URB_BUFFERED_SEND_PERIOD = 4000;
    public static final int URB_BUFFERED_WINDOW_SIZE = 256;
    public static final int URB_BUFFERED_START_DELAY_AFTER_ROUND = 500;

    // debug output
    public static final boolean DEBUG_OUTPUT_PL = false;
    public static final boolean DEBUG_OUTPUT_URB = false;
    public static final boolean DEBUG_OUTPUT_URB_RELAY = false;
    public static final boolean DEBUG_OUTPUT_URB_BUFFER = false;
    public static final boolean DEBUG_OUTPUT_FIFO = false;

    // write log file
    public static final boolean WRITE_LOG_FIFO = true;
}
