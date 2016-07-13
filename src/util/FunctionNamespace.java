package util;

public class FunctionNamespace extends Namespace{
    private int generateCounter = 0;

    public String generateNewName() {
        String retStr = String.format(
                "AM_FUNC_%d",
                //this.hashCode(),
                this.generateCounter
        );
        this.generateCounter = this.generateCounter + 1;
        this.definedNameSet.add(retStr);
        return retStr;
    }
}
