package abstractPattern.type;

public enum LoopType {
    While ("While"),
    For   ("For"),
    Any   ("*");

    private String loopNameString = "";

    LoopType(String nameStr) { this.loopNameString = nameStr; }

    public static LoopType fromString(String loopString) {
        if (loopString.equals("while")) return While;
        if (loopString.equals("for")) return For;
        if (loopString.equals("*")) return Any;
        throw new RuntimeException();
    }

    @Override public String toString() { return this.loopNameString; }
}
