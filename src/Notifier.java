import Matlab.Recognizer.INotifier;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;

public class Notifier implements INotifier{
    public void Notify(String path, IReport report) {
        System.out.print(String.format("At file: %s ", path));
        if (report.GetIsOk()) {
            System.out.println("Success");
        } else {
            System.out.println();
            int counter = 0;
            for (Message msg : report) {
                System.out.println(String.format(
                        "[%d,%d] %s",
                        msg.GetLine(),
                        msg.GetColumn(),
                        msg.GetText())
                );
                counter++;
            }
            System.out.println(String.format("Total %d errors"));
        }
    }
}
