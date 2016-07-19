package abstractPattern.analysis;

public enum PatternType {
    Primitive,
    Modifier,
    Invalid;

    public static PatternType andMerge(PatternType LHS, PatternType RHS) {
        /* AND| P | M | I  M = Modifier Pattern
           ---+---+---+--- P = Primitive Pattern
            P | P | P | I  I = Invalid Pattern
            M | P | M | I
            I | I | I | I
         */
        switch (LHS) {
            case Primitive:
                switch (RHS) {
                    case Primitive: return Primitive;
                    case Modifier:  return Primitive;
                    case Invalid:   return Invalid;
                }
            case Modifier:
                switch (RHS) {
                    case Primitive: return Primitive;
                    case Modifier:  return Modifier;
                    case Invalid:   return Invalid;
                }
            case Invalid:
                switch (RHS) {
                    case Primitive: return Invalid;
                    case Modifier:  return Invalid;
                    case Invalid:   return Invalid;
                }
        }
        throw new RuntimeException();
    }

    public static PatternType orMerge(PatternType LHS, PatternType RHS) {
       /*  OR | P | M | I  M = Modifier Pattern
           ---+---+---+--- P = Primitive Pattern
            P | P | I | I  I = Invalid Pattern
            M | I | M | I
            I | I | I | I
         */
        switch (LHS) {
            case Primitive:
                switch (RHS) {
                    case Primitive: return Primitive;
                    case Modifier:  return Invalid;
                    case Invalid:   return Invalid;
                }
            case Modifier:
                switch (RHS) {
                    case Primitive: return Invalid;
                    case Modifier:  return Modifier;
                    case Invalid:   return Invalid;
                }
            case Invalid:
                switch (RHS) {
                    case Primitive: return Invalid;
                    case Modifier:  return Invalid;
                    case Invalid:   return Invalid;
                }
        }
        throw new RuntimeException();
    }

    public static PatternType notMerge(PatternType oprand) {
        /*  NOT| P | M | I
            ---+---+---+---
               | I | M | I
         */
        switch (oprand) {
            case Primitive: return Invalid;
            case Modifier:  return Modifier;
            case Invalid:   return Invalid;
        }
        throw new RuntimeException();
    }
}
