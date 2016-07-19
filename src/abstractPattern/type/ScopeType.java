package abstractPattern.type;

public enum ScopeType {
    Function("function"),
    Script("script"),
    Class("class"),
    Aspect("aspect"),
    Loop("loop"),
    Any("*");

    private String scopeString = null;

    ScopeType(String scopeString) {
        this.scopeString = scopeString;
    }

    public static ScopeType fromString(String scopeString) {
        if (scopeString.equals("function")) return Function;
        if (scopeString.equals("script")) return Script;
        if (scopeString.equals("class")) return Class;
        if (scopeString.equals("aspect")) return Aspect;
        if (scopeString.equals("loop")) return Loop;
        if (scopeString.equals("*")) return Any;
        throw new RuntimeException();
    }

    @Override
    public String toString() {
        return this.scopeString;
    }
}
