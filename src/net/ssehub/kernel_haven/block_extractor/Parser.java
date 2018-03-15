package net.ssehub.kernel_haven.block_extractor;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeBlock;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A parser that walks through a file and returns all found {@link CodeBlock}s.
 *
 * @author Adam
 */
class Parser implements Closeable {

    private @NonNull LineNumberReader in;
    
    private @NonNull File sourceFile;
    
    private net.ssehub.kernel_haven.util.logic.parser.@NonNull Parser<@NonNull Formula> conditionParser;
    
    private @NonNull List<@NonNull CodeBlock> topBlocks;
    
    private @NonNull Deque<@NonNull CodeBlock> nesting;
    
    /**
     * Creates a parser for the given input.
     * 
     * @param in The reader to get the input from. This will be wrapped into a {@link BufferedReader}.
     * @param sourceFile The source file to specify in the {@link CodeBlock}s.
     */
    public Parser(@NonNull Reader in, @NonNull File sourceFile) {
        this.in = new LineNumberReader(in);
        this.sourceFile = sourceFile;
        this.conditionParser = new net.ssehub.kernel_haven.util.logic.parser.Parser<>(
                new CppDefinedGrammar(new VariableCache()));
        
        this.topBlocks = new LinkedList<>();
        this.nesting = new LinkedList<>();
    }
    
    /**
     * Finds all {@link CodeBlock}s in the given input.
     * 
     * @return The list of top-level blocks.
     * 
     * @throws IOException If reading the input reader fails.
     * @throws FormatException If the source file is not formatted correctly.
     */
    public @NonNull List<@NonNull CodeBlock> readBlocks() throws IOException, FormatException {
        boolean foundContentOutsideTopBlocks = false;
        
        /*
         * TODO:
         *      - foundContentOutsideTopBlocks
         *      - line continuation
         *      - #elif
         *      - #else
         *      - comments
         */
        
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            
            if (line.startsWith("#ifdef")) {
                handleIf("defined(" + line.substring("#ifdef".length()).trim() + ")");
                
            } else if (line.startsWith("#ifndef")) {
                handleIf("!defined(" + line.substring("#ifndef".length()).trim() + ")");
            
            } else if (line.startsWith("#if")) {
                handleIf(notNull(line.substring("#if".length())));
                
            } else if (line.startsWith("#elif")) {
                handleElif(notNull(line.substring("#elif".length())));
                
            } else if (line.startsWith("#else")) {
                handleElse();
                
            } else if (line.startsWith("#endif")) {
                handleEndif();
            }
        }
        
        if (!nesting.isEmpty()) {
            throw new FormatException("Found opening at line " + notNull(nesting.peek()).getLineStart()
                    + " but no closing #endif");
        }
        
        List<@NonNull CodeBlock> result;
        if (foundContentOutsideTopBlocks) {
            // if we found code outside of #ifdefs, then add a pseudo block for the whole file
            CodeBlock topElement = new CodeBlock(1, in.getLineNumber(), sourceFile, True.INSTANCE, True.INSTANCE);
            for (CodeBlock element : topBlocks) {
                topElement.addNestedElement(element);
            }
            result = notNull(Arrays.asList(topElement));
            
        } else {
            result = topBlocks;
        }
        
        return result;
    }
    
    /**
     * Handles an #if line. Called by the main parsing loop if it is determined that the current line is an #if,
     * #ifdef or #ifdef.
     * 
     * @param expression The condition expression containing defined() calls.
     * 
     * @throws FormatException If handling the #if fails.
     */
    private void handleIf(@NonNull String expression) throws FormatException {
        Formula condition;
        try {
            condition = conditionParser.parse(expression);
        } catch (ExpressionFormatException e) {
            throw new FormatException(e);
        }
        
        Formula pc;
        if (!nesting.isEmpty()) {
            pc = new Conjunction(notNull(nesting.peek()).getPresenceCondition(), condition);
        } else {
            pc = condition;
        }
        
        CodeBlock newBlock = new CodeBlock(in.getLineNumber(), -1, sourceFile, condition, pc);
        nesting.push(newBlock);
    }
    
    /**
     * Handles an #elif line. Called by the main parsing loop if it is determined that the current line is an #elif.
     * 
     * @param condition The condition expression containing defined() calls.
     * 
     * @throws FormatException If handling the #elif fails.
     */
    private void handleElif(@NonNull String condition) throws FormatException {
        throw new FormatException("#elif is currently not supported");
    }
    
    /**
     * Handles an #else line. Called by the main parsing loop if it is determined that the current line is an #else.
     * 
     * @throws FormatException If handling the #else fails.
     */
    private void handleElse() throws FormatException {
        throw new FormatException("#else are currently not supported");
    }
    
    /**
     * Handles an #endif line. Called by the main parsing loop if it is determined that the current line is an #endif.
     * 
     * @throws FormatException If handling the #endif fails.
     */
    private void handleEndif() throws FormatException {
        if (nesting.isEmpty()) {
            throw new FormatException("Found #endif with no corresponding opening in line"
                    + in.getLineNumber());
        }
        
        CodeBlock block = notNull(nesting.pop());
        
        // copy to set the end line // TODO: this is not ideal....
        List<@NonNull CodeBlock> nested = new ArrayList<>(block.getNestedElementCount());
        for (CodeBlock child : block.iterateNestedBlocks()) {
            nested.add(child);
        }
        block = new CodeBlock(block.getLineStart(), in.getLineNumber(), sourceFile, block.getCondition(),
                block.getPresenceCondition());
        for (CodeBlock child : nested) {
            block.addNestedElement(child);
        }
        
        if (nesting.isEmpty()) {
            topBlocks.add(block);
        } else {
            notNull(nesting.peek()).addNestedElement(block);
        }
    }
    
    /**
     * Closes the input reader that was passed to this parser in the constructor.
     */
    @Override
    public void close() throws IOException {
        in.close();
    }
    
}