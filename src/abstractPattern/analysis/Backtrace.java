package abstractPattern.analysis;

import Matlab.Utils.Message;
import Matlab.Utils.Severity;

public class Backtrace extends Exception {
    private Message backtraceMsg = null;

    public Backtrace(Message message) {
        this.backtraceMsg = message;
    }

    public Backtrace(String pFilepath, int pLine, int pColumn, String pText) {
        this.backtraceMsg = new Message(
                Severity.Error,
                pFilepath,
                pLine,
                pColumn,
                pText
        );
    }

    public Message getBacktraceMsg() {
        return backtraceMsg;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s][%d : %d] %s",
                this.backtraceMsg.GetSeverity(),
                this.backtraceMsg.GetLine(),
                this.backtraceMsg.GetColumn(),
                this.backtraceMsg.GetText()
        );
    }
}
