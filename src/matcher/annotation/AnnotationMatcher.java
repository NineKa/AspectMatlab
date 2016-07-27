package matcher.annotation;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;

public class AnnotationMatcher {
    private String rawComment = "";
    private boolean isValid = true;
    private AnnotateLexer lexer = null;
    private AnnotateParser parser = null;
    private AbstractAnnotation abstractAnnotation = null;

    public AnnotationMatcher(String rawComment) {
        this.rawComment = rawComment;
        try {
            this.lexer = new AnnotateLexer(new ANTLRStringStream(this.rawComment));
            this.parser = new AnnotateParser(new CommonTokenStream(this.lexer));
            this.abstractAnnotation = this.parser.annotate();
        } catch (Exception except) {
            this.isValid = false;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public AbstractAnnotation getAbstractAnnotation() {
        if (!this.isValid) throw new UnsupportedOperationException();
        return abstractAnnotation;
    }
}
