JASTADD_JAR="../tool/jastadd2/jastadd2.jar"

AST="./matcher/parameter/mex/cAST/c.ast"
PrettyPrint="./matcher/parameter/mex/cAST/prettyPrint.jrag"

java -jar $JASTADD_JAR $AST $PrettyPrint --ASTNode="MEXCGen" --package="matcher.parameter.mex.cAST"