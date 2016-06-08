/**
 * Original JavaScript Version: http://scratch-lang.notimetoplay.org/scratch-lang.js
 */
package scratch;

import java.util.HashMap;
import java.util.Stack;

class ScratchLexer {
    private String[] words;
    private int next;

    public ScratchLexer(String text) {
        words = text.split("\\s+");
        next = 0;
    }

    public String nextWord() {
        if (next >= words.length) return null;
        return words[next++];
    }
}

public class Scratch1 {
    private HashMap<String, Code> dictionary = new HashMap<>();
    public Stack stack = new Stack();

    public Scratch1() {
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
        };
        for (int i = 0; i < words.length; i += 2) {
            define((String)words[i], (Code)words[i + 1]);
        }
    }

    public void define(String name, Code code) {
        dictionary.put(name.toUpperCase(), code);
    }

    public void run(String text) {
        ScratchLexer lexer = new ScratchLexer(text);
        String word;
        double num_val;
        while ((word = lexer.nextWord()) != null) {
            word = word.toUpperCase();
            if (dictionary.containsKey(word)) {
                dictionary.get(word).call(this);
                continue;
            }
            try {
                num_val = Double.parseDouble(word);
                stack.push(num_val);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unknown word");
            }
        }
    }

    public static void main(String[] args) {
        Scratch1 terp = new Scratch1();
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
    }
}

abstract class Code {
    public abstract void call(Scratch1 terp);
}

// Print and discard top of stack.
class CodePrint extends Code {
    @Override
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
        System.out.println(terp.stack);
    }
}

class CodeAdd extends Code {
    @Override
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
        if (terp.stack.size() < 1) {
            throw new RuntimeException("Not enough items on stack");
        }
        terp.stack.pop();
    }
}

// Exchange positions of TOS and second item on stack (2OS).
class CodeSwap extends Code {
    @Override
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
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
    public void call(Scratch1 terp) {
        terp.stack.clear();
    }
}
