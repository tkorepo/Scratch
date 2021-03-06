/**
 * Original JavaScript Version: http://scratch-lang.notimetoplay.org/scratch-lang3.js
 */
package scratch;

import java.util.HashMap;
import java.util.Stack;

class ScratchLexer {
    private String text;
    private int position; // Beginning of TEXT.

    public ScratchLexer(String text) {
        this.text = text;
        position = 0;
    }

    // Trying to avoid regular expressions here.
    public boolean isWhitespace(char ch) {
        return ch == ' '
            || ch == '\t'
            || ch == '\r'
            || ch == '\n';
    }

    public String nextWord() {
        if (position >= text.length()) {
            return null;
        }
        while (isWhitespace(text.charAt(position))) {
            position++;
            if (position >= text.length()) {
                return null;
            }
        }
        int new_pos = position;
        while (!isWhitespace(text.charAt(new_pos))) {
            new_pos++;
            if (new_pos >= text.length()) {
                break;
            }
        }
        String collector = text.substring(position, new_pos);
        new_pos++;
        position = new_pos; // Skip the delimiter.
        return collector;
    }

    public String nextCharsUpTo(char ch) {
        if (position >= text.length()) {
            return null;
        }
        int new_pos = position;
        while (text.charAt(new_pos) != ch) {
            new_pos++;
            if (new_pos >= text.length()) {
                throw new RuntimeException("Unexpected end of input");
            }
        }
        String collector = text.substring(position, new_pos);
        new_pos++;
        position = new_pos; // Skip the delimiter.
        return collector;
    }
}

public class Scratch3 {
    private HashMap<String, Code> dictionary = new HashMap<>();
    private Stack data_stack = new Stack();
    private Stack compile_buffer = new Stack();
    public Stack stack = data_stack;
    private boolean immediate = false;
    public ScratchLexer lexer;
    public String latest;

    public Scratch3() {
        Object[] words = {
            "PRINT", new CodePrint(),
            ".", new CodePrint(),
            "PSTACK", new CodePstack(),
            ".S", new CodePstack(),
            "+", new CodeAdd(),
            "-", new CodeSub(),
            "*", new CodeMul(),
            "/", new CodeDiv(),
            "SQRT", new CodeSqrt(),
            "DUP", new CodeDup(),
            "DROP", new CodeDrop(),
            "SWAP", new CodeSwap(),
            "OVER", new CodeOver(),
            "ROT", new CodeRot(),
            "CLEAR", new CodeClear(),
            "VAR", new CodeVar(),
            "STORE", new CodeStore(),
            "!", new CodeStore(),
            "FETCH", new CodeFetch(),
            "@", new CodeFetch(),
            "CONST", new CodeConst(),
            "\"", new CodeString(),
            "/*", new CodeCComment(),
            "(", new CodeComment(),
            "//", new CodeCCComment(),
            "DEF", new CodeDef(),
            ":", new CodeDef(),
            "END", new CodeEnd(),
            ";", new CodeEnd(),
        };
        for (int i = 0; i < words.length; i += 2) {
            define((String)words[i], (Code)words[i + 1]);
        }
    }

    public void define(String word, Code code) {
        dictionary.put(word.toUpperCase(), code);
    }

    public void run(String text) {
        lexer = new ScratchLexer(text);
        String word;
        while ((word = lexer.nextWord()) != null) {
            Object obj = compile(word);
            if (immediate) {
                interpret(obj);
                immediate = false;
            } else if (isCompiling()) {
                stack.push(obj);
            } else {
                interpret(obj);
            }
        }
    }

    private Object compile(String word) {
        word = word.toUpperCase();
        if (dictionary.containsKey(word)) {
            immediate = ((Code)dictionary.get(word)).immediate;
            return dictionary.get(word);
        }
        try {
            return Double.parseDouble(word);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unknown word: [" + word + "]");
        }
    }

    public void interpret(Object word) {
        if (word instanceof Code) {
            ((Code)word).call(this);
        } else {
            stack.push(word);
        }
    }

    public void startCompiling() {
        stack = compile_buffer;
    }

    public void stopCompiling() {
        stack = data_stack;
    }

    private boolean isCompiling() {
        return stack == compile_buffer;
    }

    public static void main(String[] args) {
        Scratch3 terp = new Scratch3();
        terp.run("1 2 + print");
        terp.run("3 4 - print");
        terp.run("5 6 * print");
        terp.run("7 8 / print");
        terp.run("9 sqrt print");
        terp.run("10 dup pstack clear");
        terp.run("11 drop pstack");
        terp.run("12 13 swap pstack clear");
        terp.run("14 15 over pstack clear");
        terp.run("16 17 18 rot pstack clear");
        terp.run("var a 19 a ! a @ print");
        terp.run("20 const b b print");
        terp.run("\" 21\" print");
        terp.run("22 /* comment */ print");
        terp.run("23 ( comment ) print");
        terp.run("24 // comment\n print");
        terp.run(": c 25 print ; c");
        terp.run("pstack");
    }
}

abstract class Code {
    public boolean immediate;

    public abstract void call(Scratch3 terp);
}

abstract class CodeImmediate extends Code {
    public CodeImmediate() {
        immediate = true;
    }
}

// Print and discard top of stack.
class CodePrint extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        Object tos = terp.stack.pop();
        System.out.println(tos);
    }
}

// Print out the contents of the stack.
class CodePstack extends Code {
    @Override
    public void call(Scratch3 terp) {
        System.out.println(terp.stack);
    }
}

class CodeAdd extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        double tos = (double)terp.stack.pop();
        double _2os = (double)terp.stack.pop();
        terp.stack.push(_2os + tos);
    }
}

class CodeSub extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        double tos = (double)terp.stack.pop();
        double _2os = (double)terp.stack.pop();
        terp.stack.push(_2os - tos);
    }
}

class CodeMul extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        double tos = (double)terp.stack.pop();
        double _2os = (double)terp.stack.pop();
        terp.stack.push(_2os * tos);
    }
}

class CodeDiv extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        double tos = (double)terp.stack.pop();
        double _2os = (double)terp.stack.pop();
        terp.stack.push(_2os / tos);
    }
}

class CodeSqrt extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        double tos = (double)terp.stack.pop();
        terp.stack.push(Math.sqrt(tos));
    }
}

// Duplicate the top of stack (TOS).
class CodeDup extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        Object tos = terp.stack.pop();
        terp.stack.push(tos);
        terp.stack.push(tos);
    }
}

// Throw away the TOS -- the opposite of DUP.
class CodeDrop extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        terp.stack.pop();
    }
}

// Exchange positions of TOS and second item on stack (2OS).
class CodeSwap extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        Object tos = terp.stack.pop();
        Object _2os = terp.stack.pop();
        terp.stack.push(tos);
        terp.stack.push(_2os);
    }
}

// Copy 2OS on top of stack.
class CodeOver extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        Object tos = terp.stack.pop();
        Object _2os = terp.stack.pop();
        terp.stack.push(_2os);
        terp.stack.push(tos);
        terp.stack.push(_2os);
    }
}

// Bring the 3rd item on stack to the top.
class CodeRot extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 3) {
            throw new RuntimeException("Not enough items on stack");
        }
        Object tos = terp.stack.pop();
        Object _2os = terp.stack.pop();
        Object _3os = terp.stack.pop();
        terp.stack.push(_2os);
        terp.stack.push(tos);
        terp.stack.push(_3os);
    }
}

class CodeClear extends Code {
    @Override
    public void call(Scratch3 terp) {
        terp.stack.clear();
    }
}

class CodeVarRef extends Code {
    public Object value;

    @Override
    public void call(Scratch3 terp) {
        terp.stack.push(this);
    }
}

// Read next word from input and make it a variable.
class CodeVar extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        String var_name = terp.lexer.nextWord();
        if (var_name == null) {
            throw new RuntimeException("Unexpected end of input");
        }
        terp.define(var_name, new CodeVarRef());
    }
}

// Store value of 2OS into variable given by TOS.
class CodeStore extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 2) {
            throw new RuntimeException("Not enough items on stack");
        }
        CodeVarRef reference = (CodeVarRef)terp.stack.pop();
        Object new_value = terp.stack.pop();
        reference.value = new_value;
    }
}

// Replace reference to variable on TOS with its value.
class CodeFetch extends Code {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        CodeVarRef reference = (CodeVarRef)terp.stack.pop();
        terp.stack.push(reference.value);
    }
}

class CodeConstRef extends Code {
    private final Object value;

    public CodeConstRef(Object value) {
        this.value = value;
    }

    @Override
    public void call(Scratch3 terp) {
        terp.stack.push(value);
    }
}

// Read next word from input and make it a constant with TOS as value.
class CodeConst extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        String const_name = terp.lexer.nextWord();
        if (const_name == null) {
            throw new RuntimeException("Unexpected end of input");
        }
        Object const_value = terp.stack.pop();
        terp.define(const_name, new CodeConstRef(const_value));
    }
}

class CodeString extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        terp.stack.push(terp.lexer.nextCharsUpTo('"'));
    }
}

class CodeCComment extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        String next_word;
        do {
            next_word = terp.lexer.nextWord();
            if (next_word == null) {
                throw new RuntimeException("Unexpected end of input");
            }
        } while (!next_word.endsWith("*/"));
    }
}

class CodeComment extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        terp.lexer.nextCharsUpTo(')');
    }
}

class CodeCCComment extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        terp.lexer.nextCharsUpTo('\n');
    }
}

class CodeWordRef extends Code {
    private Stack code;

    public CodeWordRef(Stack code) {
        this.code = code;
    }

    @Override
    public void call(Scratch3 terp) {
        int code_pointer = 0;
        while (code_pointer < code.size()) {
            terp.interpret(code.get(code_pointer));
            code_pointer++;
        }
    }
}

class CodeDef extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        String new_word = terp.lexer.nextWord();
        if (new_word == null) {
            throw new RuntimeException("Unexpected end of input");
        }
        terp.latest = new_word;
        terp.startCompiling();
    }
}

class CodeEnd extends CodeImmediate {
    @Override
    public void call(Scratch3 terp) {
        Stack new_code = new Stack();
        new_code.addAll(terp.stack); // Clone compile_buffer.
        terp.stack.clear(); // Clear compile_buffer.
        terp.define(terp.latest, new CodeWordRef(new_code));
        terp.stopCompiling();
    }
}
