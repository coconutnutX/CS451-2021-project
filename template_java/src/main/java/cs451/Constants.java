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
    public static final int PL_THREADPOOL_SIZE = 4;
    public static final int URB_BUFFERED_SEND_PERIOD = 500;
    public static final int URB_BUFFERED_WINDOW_SIZE = 256;
    public static final boolean ACTIVATE_URB_BUFFER = false;
    public static final int FIFO_MSG_THRESHOLD = 128;

    // lcb
    public static final int LCB_MSG_THRESHOLD = 128;
    public static final int LCB_CHECK_PERIOD = 60 * 1000;

    // debug output
    public static final boolean DEBUG_OUTPUT_PL = false;
    public static final boolean DEBUG_OUTPUT_URB = false;
    public static final boolean DEBUG_OUTPUT_URB_RELAY = false;
    public static final boolean DEBUG_OUTPUT_URB_BUFFER = false;
    public static final boolean DEBUG_OUTPUT_FIFO = false;
    public static final boolean DEBUG_OUTPUT_LCB= false;
    public static final boolean DEBUG_OUTPUT_LCB_CHECK= false;

    // write log file
    public static final boolean WRITE_LOG_FIFO = false;
    public static final boolean WRITE_LOG_LCB = true;
}
