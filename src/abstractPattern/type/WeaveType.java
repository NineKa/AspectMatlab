package abstractPattern.type;

public enum WeaveType {
    Before,
    After,
    Around;

    public static WeaveType fromString(String string) {
        if (string.equals("before")) return Before;
        if (string.equals("after")) return After;
        if (string.equals("around")) return Around;
        /* control flow should not reach here */
        throw new RuntimeException();
    }

    @Override
    public String toString() {
        switch (this) {
            case Before: return "before";
            case After:  return "after";
            case Around: return "around";
        }
        /* control flow should not reach here */
        throw new RuntimeException();
    }
}
