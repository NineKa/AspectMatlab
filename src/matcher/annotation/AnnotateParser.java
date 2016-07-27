// $ANTLR 3.4 Annotate.g3 2016-07-26 20:27:24

	package matcher.annotation;
	import ast.*;
	import natlab.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class AnnotateParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "DIGIT", "EXP", "ID", "IMAGINARY", "IMG", "LETTER", "NUMBER", "REAL", "SIGN", "STRING", "WHITESPACE", "'%@'", "','", "'['", "']'"
    };

    public static final int EOF=-1;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int DIGIT=4;
    public static final int EXP=5;
    public static final int ID=6;
    public static final int IMAGINARY=7;
    public static final int IMG=8;
    public static final int LETTER=9;
    public static final int NUMBER=10;
    public static final int REAL=11;
    public static final int SIGN=12;
    public static final int STRING=13;
    public static final int WHITESPACE=14;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public AnnotateParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public AnnotateParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return AnnotateParser.tokenNames; }
    public String getGrammarFileName() { return "Annotate.g3"; }


    	private ast.List<Expr> selectorLBuffer = new ast.List<>();
    	private ast.List<ast.List<Expr>> annotateBuffer = new ast.List();

        @Override
        public void reportError(RecognitionException e){
            throw new RuntimeException();
        }



    // $ANTLR start "tNum"
    // Annotate.g3:27:1: tNum returns [Expr expr] : ( REAL | IMAGINARY );
    public final Expr tNum() throws RecognitionException {
        Expr expr = null;


        Token REAL1=null;
        Token IMAGINARY2=null;

        try {
            // Annotate.g3:28:4: ( REAL | IMAGINARY )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==REAL) ) {
                alt1=1;
            }
            else if ( (LA1_0==IMAGINARY) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }
            switch (alt1) {
                case 1 :
                    // Annotate.g3:28:6: REAL
                    {
                    REAL1=(Token)match(input,REAL,FOLLOW_REAL_in_tNum54); 


                    				boolean isFloatPointNum = true;
                    				String tempStr = (REAL1!=null?REAL1.getText():null) ;
                    				if (tempStr.indexOf('.') != -1) isFloatPointNum = false; 
                    				if (isFloatPointNum) {
                    					expr = new IntLiteralExpr(new DecIntNumericLiteralValue((REAL1!=null?REAL1.getText():null), false));
                    				} else {
                    					expr = new FPLiteralExpr(new FPNumericLiteralValue((REAL1!=null?REAL1.getText():null), false));
                    				}
                    			

                    }
                    break;
                case 2 :
                    // Annotate.g3:38:7: IMAGINARY
                    {
                    IMAGINARY2=(Token)match(input,IMAGINARY,FOLLOW_IMAGINARY_in_tNum64); 


                    				boolean isFloatPointNum = true;
                    				String tempStr = (IMAGINARY2!=null?IMAGINARY2.getText():null) ;
                    				if (tempStr.indexOf('.') != -1) isFloatPointNum = false; 
                    				if (isFloatPointNum) {
                    					expr = new IntLiteralExpr(new DecIntNumericLiteralValue((IMAGINARY2!=null?IMAGINARY2.getText():null), true));
                    				} else {
                    					expr = new FPLiteralExpr(new FPNumericLiteralValue((IMAGINARY2!=null?IMAGINARY2.getText():null), true));
                    				}
                    			

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "tNum"



    // $ANTLR start "tStr"
    // Annotate.g3:48:1: tStr returns [Expr expr] : STRING ;
    public final Expr tStr() throws RecognitionException {
        Expr expr = null;


        Token STRING3=null;

        try {
            // Annotate.g3:49:4: ( STRING )
            // Annotate.g3:49:6: STRING
            {
            STRING3=(Token)match(input,STRING,FOLLOW_STRING_in_tStr83); 


            				expr = new StringLiteralExpr((STRING3!=null?STRING3.getText():null));
            			

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "tStr"



    // $ANTLR start "tVar"
    // Annotate.g3:52:1: tVar returns [Expr expr] : ID ;
    public final Expr tVar() throws RecognitionException {
        Expr expr = null;


        Token ID4=null;

        try {
            // Annotate.g3:53:4: ( ID )
            // Annotate.g3:53:6: ID
            {
            ID4=(Token)match(input,ID,FOLLOW_ID_in_tVar101); 


            				expr = new NameExpr(new Name((ID4!=null?ID4.getText():null)));
            			

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "tVar"



    // $ANTLR start "selectorE"
    // Annotate.g3:57:1: selectorE returns [Expr expr] : ( tNum | tStr | tVar );
    public final Expr selectorE() throws RecognitionException {
        Expr expr = null;


        Expr tNum5 =null;

        Expr tStr6 =null;

        Expr tVar7 =null;


        try {
            // Annotate.g3:58:4: ( tNum | tStr | tVar )
            int alt2=3;
            switch ( input.LA(1) ) {
            case IMAGINARY:
            case REAL:
                {
                alt2=1;
                }
                break;
            case STRING:
                {
                alt2=2;
                }
                break;
            case ID:
                {
                alt2=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // Annotate.g3:58:6: tNum
                    {
                    pushFollow(FOLLOW_tNum_in_selectorE120);
                    tNum5=tNum();

                    state._fsp--;


                     expr = tNum5; 

                    }
                    break;
                case 2 :
                    // Annotate.g3:59:7: tStr
                    {
                    pushFollow(FOLLOW_tStr_in_selectorE130);
                    tStr6=tStr();

                    state._fsp--;


                     expr = tStr6; 

                    }
                    break;
                case 3 :
                    // Annotate.g3:60:7: tVar
                    {
                    pushFollow(FOLLOW_tVar_in_selectorE140);
                    tVar7=tVar();

                    state._fsp--;


                     expr = tVar7; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "selectorE"



    // $ANTLR start "selectorL"
    // Annotate.g3:62:1: selectorL returns [ast.List<Expr> exprs] : '[' (e= selectorE ( ',' a= selectorE )* )? ']' ;
    public final ast.List<Expr> selectorL() throws RecognitionException {
        ast.List<Expr> exprs = null;


        Expr e =null;

        Expr a =null;


        try {
            // Annotate.g3:63:4: ( '[' (e= selectorE ( ',' a= selectorE )* )? ']' )
            // Annotate.g3:63:6: '[' (e= selectorE ( ',' a= selectorE )* )? ']'
            {
            match(input,17,FOLLOW_17_in_selectorL158); 

            // Annotate.g3:64:6: (e= selectorE ( ',' a= selectorE )* )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0 >= ID && LA4_0 <= IMAGINARY)||LA4_0==REAL||LA4_0==STRING) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // Annotate.g3:65:7: e= selectorE ( ',' a= selectorE )*
                    {
                    pushFollow(FOLLOW_selectorE_in_selectorL176);
                    e=selectorE();

                    state._fsp--;


                    selectorLBuffer.add(e);

                    // Annotate.g3:66:7: ( ',' a= selectorE )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==16) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // Annotate.g3:66:8: ',' a= selectorE
                    	    {
                    	    match(input,16,FOLLOW_16_in_selectorL188); 

                    	    pushFollow(FOLLOW_selectorE_in_selectorL192);
                    	    a=selectorE();

                    	    state._fsp--;


                    	    selectorLBuffer.add(a);

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);


                    }
                    break;

            }


            match(input,18,FOLLOW_18_in_selectorL211); 

             
            				exprs = selectorLBuffer;
            				selectorLBuffer = new ast.List<>();
            			

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return exprs;
    }
    // $ANTLR end "selectorL"



    // $ANTLR start "selector"
    // Annotate.g3:73:1: selector returns [ast.List<Expr> exprs] : ( selectorE | selectorL );
    public final ast.List<Expr> selector() throws RecognitionException {
        ast.List<Expr> exprs = null;


        Expr selectorE8 =null;

        ast.List<Expr> selectorL9 =null;


        try {
            // Annotate.g3:74:4: ( selectorE | selectorL )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0 >= ID && LA5_0 <= IMAGINARY)||LA5_0==REAL||LA5_0==STRING) ) {
                alt5=1;
            }
            else if ( (LA5_0==17) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // Annotate.g3:74:6: selectorE
                    {
                    pushFollow(FOLLOW_selectorE_in_selector229);
                    selectorE8=selectorE();

                    state._fsp--;


                     exprs = new ast.List<>(selectorE8); 

                    }
                    break;
                case 2 :
                    // Annotate.g3:75:7: selectorL
                    {
                    pushFollow(FOLLOW_selectorL_in_selector239);
                    selectorL9=selectorL();

                    state._fsp--;


                     exprs = selectorL9; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return exprs;
    }
    // $ANTLR end "selector"



    // $ANTLR start "annotate"
    // Annotate.g3:77:1: annotate returns [AbstractAnnotation absAnnotate] : '%@' ID (e= selector (a= selector )* )? ;
    public final AbstractAnnotation annotate() throws RecognitionException {
        AbstractAnnotation absAnnotate = null;


        Token ID10=null;
        ast.List<Expr> e =null;

        ast.List<Expr> a =null;


        try {
            // Annotate.g3:78:4: ( '%@' ID (e= selector (a= selector )* )? )
            // Annotate.g3:78:6: '%@' ID (e= selector (a= selector )* )?
            {
            match(input,15,FOLLOW_15_in_annotate256); 

            ID10=(Token)match(input,ID,FOLLOW_ID_in_annotate258); 

            // Annotate.g3:79:5: (e= selector (a= selector )* )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0 >= ID && LA7_0 <= IMAGINARY)||LA7_0==REAL||LA7_0==STRING||LA7_0==17) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // Annotate.g3:80:6: e= selector (a= selector )*
                    {
                    pushFollow(FOLLOW_selector_in_annotate274);
                    e=selector();

                    state._fsp--;


                    annotateBuffer.add(e);

                    // Annotate.g3:81:6: (a= selector )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0 >= ID && LA6_0 <= IMAGINARY)||LA6_0==REAL||LA6_0==STRING||LA6_0==17) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // Annotate.g3:81:7: a= selector
                    	    {
                    	    pushFollow(FOLLOW_selector_in_annotate287);
                    	    a=selector();

                    	    state._fsp--;


                    	    annotateBuffer.add(a);

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);


                    }
                    break;

            }



            				absAnnotate = new AbstractAnnotation((ID10!=null?ID10.getText():null), annotateBuffer);
            			

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return absAnnotate;
    }
    // $ANTLR end "annotate"

    // Delegated rules


 

    public static final BitSet FOLLOW_REAL_in_tNum54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGINARY_in_tNum64 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_tStr83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_tVar101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tNum_in_selectorE120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tStr_in_selectorE130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tVar_in_selectorE140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_selectorL158 = new BitSet(new long[]{0x00000000000428C0L});
    public static final BitSet FOLLOW_selectorE_in_selectorL176 = new BitSet(new long[]{0x0000000000050000L});
    public static final BitSet FOLLOW_16_in_selectorL188 = new BitSet(new long[]{0x00000000000028C0L});
    public static final BitSet FOLLOW_selectorE_in_selectorL192 = new BitSet(new long[]{0x0000000000050000L});
    public static final BitSet FOLLOW_18_in_selectorL211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorE_in_selector229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorL_in_selector239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_15_in_annotate256 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_annotate258 = new BitSet(new long[]{0x00000000000228C2L});
    public static final BitSet FOLLOW_selector_in_annotate274 = new BitSet(new long[]{0x00000000000228C2L});
    public static final BitSet FOLLOW_selector_in_annotate287 = new BitSet(new long[]{0x00000000000228C2L});

}