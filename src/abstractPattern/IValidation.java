package abstractPattern;

import Matlab.Utils.IReport;

public interface IValidation {
    public boolean isValid();
    public IReport getValidationReport(String pFilePath);
}
