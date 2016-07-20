package abstractPattern.type;

public enum OperatorType {
    Plus    (2),       /* "+"  */
    Minus   (2),       /* "-"  */
    mTimes  (2),       /* "*"  */
    Times   (2),       /* ".*" */
    mrDivide(2),       /* "/"  */
    rDivide (2),       /* "./" */
    mlDivide(2),       /* "\"  */
    lDivide (2),       /* ".\" */
    mPower  (2),       /* "^"  */
    Power   (2),       /* ".^" */
    Transpose(1)       /* ".'" */;

    private int maxArgs = 0;

    OperatorType (int pMaxArgs) { this.maxArgs = pMaxArgs; }
    public int getMaximumArgumentNumber() { return this.maxArgs; }

    public static OperatorType fromString(String typeString) {
        if (typeString.equals("+"))   return OperatorType.Plus;
        if (typeString.equals("-"))   return OperatorType.Minus;
        if (typeString.equals("*"))   return OperatorType.mTimes;
        if (typeString.equals(".*"))  return OperatorType.Times;
        if (typeString.equals("/"))   return OperatorType.mrDivide;
        if (typeString.equals("./"))  return OperatorType.rDivide;
        if (typeString.equals("\\"))  return OperatorType.mlDivide;
        if (typeString.equals(".\\")) return OperatorType.lDivide;
        if (typeString.equals("^"))   return OperatorType.mPower;
        if (typeString.equals(".^"))  return OperatorType.Power;
        if (typeString.equals(".'"))   return OperatorType.Transpose;
        throw new RuntimeException();
    }

    public boolean isUniaryOperator() { return this.maxArgs == 1; }

    @Override public String toString() {
        switch (this) {
            case Plus:     return "+";
            case Minus:    return "-";
            case mTimes:   return "*";
            case Times:    return ".*";
            case mrDivide: return "/";
            case rDivide:  return "./";
            case mlDivide: return "\\";
            case lDivide:  return ".\\";
            case mPower:   return "^";
            case Power:    return ".^";
            case Transpose:return ".'";
            default:       throw new RuntimeException();
        }
    }
}