package util;

public class VarNamespace extends Namespace{
    private int generateCounter = 0;

    public String generateNewName() {
        String retStr = String.format(
                "AM_VAR_%d",
                //this.hashCode(),
                this.generateCounter
        );
        this.generateCounter = this.generateCounter + 1;
        this.definedNameSet.add(retStr);
        return retStr;
    }
}
