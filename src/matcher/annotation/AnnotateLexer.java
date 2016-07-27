// $ANTLR 3.4 Annotate.g3 2016-07-26 20:27:24

    package matcher.annotation;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class AnnotateLexer extends Lexer {
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

        @Override
        public void reportError(RecognitionException e){
            throw new RuntimeException();
        }


    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public AnnotateLexer() {} 
    public AnnotateLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public AnnotateLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "Annotate.g3"; }

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:12:7: ( '%@' )
            // Annotate.g3:12:9: '%@'
            {
            match("%@"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:13:7: ( ',' )
            // Annotate.g3:13:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:14:7: ( '[' )
            // Annotate.g3:14:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:15:7: ( ']' )
            // Annotate.g3:15:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:86:13: ( ( '\\t' | ' ' | '\\u000C' )+ )
            // Annotate.g3:86:16: ( '\\t' | ' ' | '\\u000C' )+
            {
            // Annotate.g3:86:16: ( '\\t' | ' ' | '\\u000C' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\t'||LA1_0=='\f'||LA1_0==' ') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Annotate.g3:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)=='\f'||input.LA(1)==' ' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WHITESPACE"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:87:10: ( '\\'' ( '\\'\\'' |~ ( '\\'' | '\\r' | '\\n' ) )* '\\'' )
            // Annotate.g3:87:13: '\\'' ( '\\'\\'' |~ ( '\\'' | '\\r' | '\\n' ) )* '\\''
            {
            match('\''); 

            // Annotate.g3:87:18: ( '\\'\\'' |~ ( '\\'' | '\\r' | '\\n' ) )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\'') ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1=='\'') ) {
                        alt2=1;
                    }


                }
                else if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '\f')||(LA2_0 >= '\u000E' && LA2_0 <= '&')||(LA2_0 >= '(' && LA2_0 <= '\uFFFF')) ) {
                    alt2=2;
                }


                switch (alt2) {
            	case 1 :
            	    // Annotate.g3:87:19: '\\'\\''
            	    {
            	    match("''"); 



            	    }
            	    break;
            	case 2 :
            	    // Annotate.g3:87:28: ~ ( '\\'' | '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "REAL"
    public final void mREAL() throws RecognitionException {
        try {
            int _type = REAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:88:8: ( NUMBER )
            // Annotate.g3:88:11: NUMBER
            {
            mNUMBER(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "REAL"

    // $ANTLR start "IMAGINARY"
    public final void mIMAGINARY() throws RecognitionException {
        try {
            int _type = IMAGINARY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:89:12: ( NUMBER IMG )
            // Annotate.g3:89:15: NUMBER IMG
            {
            mNUMBER(); 


            mIMG(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IMAGINARY"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Annotate.g3:90:7: ( LETTER ( '_' | LETTER | DIGIT )* )
            // Annotate.g3:90:10: LETTER ( '_' | LETTER | DIGIT )*
            {
            mLETTER(); 


            // Annotate.g3:90:17: ( '_' | LETTER | DIGIT )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'Z')||LA3_0=='_'||(LA3_0 >= 'a' && LA3_0 <= 'z')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // Annotate.g3:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // Annotate.g3:92:19: ( 'a' .. 'z' | 'A' .. 'Z' )
            // Annotate.g3:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // Annotate.g3:93:19: ( '0' .. '9' )
            // Annotate.g3:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "EXP"
    public final void mEXP() throws RecognitionException {
        try {
            // Annotate.g3:94:17: ( 'e' | 'E' | 'd' | 'D' )
            // Annotate.g3:
            {
            if ( (input.LA(1) >= 'D' && input.LA(1) <= 'E')||(input.LA(1) >= 'd' && input.LA(1) <= 'e') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXP"

    // $ANTLR start "IMG"
    public final void mIMG() throws RecognitionException {
        try {
            // Annotate.g3:95:17: ( 'i' | 'j' )
            // Annotate.g3:
            {
            if ( (input.LA(1) >= 'i' && input.LA(1) <= 'j') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IMG"

    // $ANTLR start "SIGN"
    public final void mSIGN() throws RecognitionException {
        try {
            // Annotate.g3:96:18: ( '+' | '-' )
            // Annotate.g3:
            {
            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SIGN"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            // Annotate.g3:97:19: ( ( ( DIGIT )+ ( '.' ( DIGIT )* )? | '.' ( DIGIT )+ ) ( EXP ( SIGN )? ( DIGIT )+ )? )
            // Annotate.g3:97:22: ( ( DIGIT )+ ( '.' ( DIGIT )* )? | '.' ( DIGIT )+ ) ( EXP ( SIGN )? ( DIGIT )+ )?
            {
            // Annotate.g3:97:22: ( ( DIGIT )+ ( '.' ( DIGIT )* )? | '.' ( DIGIT )+ )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0 >= '0' && LA8_0 <= '9')) ) {
                alt8=1;
            }
            else if ( (LA8_0=='.') ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // Annotate.g3:97:23: ( DIGIT )+ ( '.' ( DIGIT )* )?
                    {
                    // Annotate.g3:97:23: ( DIGIT )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // Annotate.g3:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);


                    // Annotate.g3:97:32: ( '.' ( DIGIT )* )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='.') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // Annotate.g3:97:33: '.' ( DIGIT )*
                            {
                            match('.'); 

                            // Annotate.g3:97:37: ( DIGIT )*
                            loop5:
                            do {
                                int alt5=2;
                                int LA5_0 = input.LA(1);

                                if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
                                    alt5=1;
                                }


                                switch (alt5) {
                            	case 1 :
                            	    // Annotate.g3:
                            	    {
                            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                            	        input.consume();
                            	    }
                            	    else {
                            	        MismatchedSetException mse = new MismatchedSetException(null,input);
                            	        recover(mse);
                            	        throw mse;
                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop5;
                                }
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Annotate.g3:97:50: '.' ( DIGIT )+
                    {
                    match('.'); 

                    // Annotate.g3:97:54: ( DIGIT )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0 >= '0' && LA7_0 <= '9')) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // Annotate.g3:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    }
                    break;

            }


            // Annotate.g3:97:64: ( EXP ( SIGN )? ( DIGIT )+ )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0 >= 'D' && LA11_0 <= 'E')||(LA11_0 >= 'd' && LA11_0 <= 'e')) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // Annotate.g3:97:65: EXP ( SIGN )? ( DIGIT )+
                    {
                    mEXP(); 


                    // Annotate.g3:97:69: ( SIGN )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='+'||LA9_0=='-') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // Annotate.g3:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();
                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;
                            }


                            }
                            break;

                    }


                    // Annotate.g3:97:77: ( DIGIT )+
                    int cnt10=0;
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0 >= '0' && LA10_0 <= '9')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // Annotate.g3:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt10 >= 1 ) break loop10;
                                EarlyExitException eee =
                                    new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);


                    }
                    break;

            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NUMBER"

    public void mTokens() throws RecognitionException {
        // Annotate.g3:1:8: ( T__15 | T__16 | T__17 | T__18 | WHITESPACE | STRING | REAL | IMAGINARY | ID )
        int alt12=9;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // Annotate.g3:1:10: T__15
                {
                mT__15(); 


                }
                break;
            case 2 :
                // Annotate.g3:1:16: T__16
                {
                mT__16(); 


                }
                break;
            case 3 :
                // Annotate.g3:1:22: T__17
                {
                mT__17(); 


                }
                break;
            case 4 :
                // Annotate.g3:1:28: T__18
                {
                mT__18(); 


                }
                break;
            case 5 :
                // Annotate.g3:1:34: WHITESPACE
                {
                mWHITESPACE(); 


                }
                break;
            case 6 :
                // Annotate.g3:1:45: STRING
                {
                mSTRING(); 


                }
                break;
            case 7 :
                // Annotate.g3:1:52: REAL
                {
                mREAL(); 


                }
                break;
            case 8 :
                // Annotate.g3:1:57: IMAGINARY
                {
                mIMAGINARY(); 


                }
                break;
            case 9 :
                // Annotate.g3:1:67: ID
                {
                mID(); 


                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\7\uffff\1\14\2\uffff\1\14\3\uffff\2\14\1\uffff\1\14";
    static final String DFA12_eofS =
        "\22\uffff";
    static final String DFA12_minS =
        "\1\11\6\uffff\1\56\1\60\1\uffff\1\60\1\53\2\uffff\4\60";
    static final String DFA12_maxS =
        "\1\172\6\uffff\1\152\1\71\1\uffff\1\152\1\71\2\uffff\2\152\1\71"+
        "\1\152";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\11\2\uffff\1\7\1\10\4"+
        "\uffff";
    static final String DFA12_specialS =
        "\22\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\5\2\uffff\1\5\23\uffff\1\5\4\uffff\1\1\1\uffff\1\6\4\uffff"+
            "\1\2\1\uffff\1\10\1\uffff\12\7\7\uffff\32\11\1\3\1\uffff\1\4"+
            "\3\uffff\32\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\12\1\uffff\12\7\12\uffff\2\13\36\uffff\2\13\3\uffff\2\15",
            "\12\16",
            "",
            "\12\17\12\uffff\2\13\36\uffff\2\13\3\uffff\2\15",
            "\1\20\1\uffff\1\20\2\uffff\12\21",
            "",
            "",
            "\12\16\12\uffff\2\13\36\uffff\2\13\3\uffff\2\15",
            "\12\17\12\uffff\2\13\36\uffff\2\13\3\uffff\2\15",
            "\12\21",
            "\12\21\57\uffff\2\15"
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__15 | T__16 | T__17 | T__18 | WHITESPACE | STRING | REAL | IMAGINARY | ID );";
        }
    }
 

}