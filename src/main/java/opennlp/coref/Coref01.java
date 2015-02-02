package opennlp.coref;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

// https://issues.apache.org/jira/browse/OPENNLP-48
// http://blog.dpdearing.com/2012/11/making-coreference-resolution-with-opennlp-1-5-0-your-bitch/
public class Coref01 {

    private Linker linker;
    private SentenceDetector sentenceDetector;
    private Tokenizer tokenizer;
    
    private Parser parser;

    public Coref01(Linker linker) throws Exception {
        this.linker = linker;

        SentenceModel sdModel = new SentenceModel(new File("/home/kinow/nlp/models/sentdetect/en-sent.bin"));
        sentenceDetector = new SentenceDetectorME(sdModel);

        TokenizerModel tkModel = new TokenizerModel(new File(
                "/home/kinow/nlp/models/tokenize/en-token.bin"));
        tokenizer = new TokenizerME(tkModel);
        
    }

    public static void main(String[] args) throws Exception {

        String example = "Pierre Vinken, 61 years old, will join the board as a "
                + "nonexecutive director Nov. 29. Mr. Vinken is chairman of Elsevier "
                + "N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and "
                + "former chairman of Consolidated Gold Fields PLC, was named a "
                + "director of this British industrial conglomerate.";

        Linker linker = new DefaultLinker("/home/kinow/nlp/models/coref/",
                LinkerMode.TEST);
        Coref01 cf = new Coref01(linker);

        cf.doCoref(example);

        System.out.println("OK!");
    }

    private void doCoref(String text) {
        String[] sentences = sentenceDetector.sentDetect(text);

        String[][] tokens = new String[sentences.length][];
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i];
            tokens[i] = tokenizer.tokenize(sentence);
        }

        DiscourseEntity[] des = this.findEntityMentions(sentences, tokens);
        for (DiscourseEntity de : des) {
            System.out.println(de);
        }
    }

    private Parse parseSentence(final String text) {
        final Parse p = new Parse(text,
        // a new span covering the entire text
                new Span(0, text.length()),
                // the label for the top if an incomplete node
                AbstractBottomUpParser.INC_NODE,
                // the probability of this parse...uhhh...?
                1,
                // the token index of the head of this parse
                0);

        // make sure to initialize the _tokenizer correctly
        final Span[] spans = tokenizer.tokenizePos(text);

        for (int idx = 0; idx < spans.length; idx++) {
            final Span span = spans[idx];
            // flesh out the parse with individual token sub-parses
            p.insert(new Parse(text, span, AbstractBottomUpParser.TOK_NODE, 0,
                    idx));
        }

        Parse actualParse = parse(p);
        return actualParse;
    }

    private Parse parse(final Parse p) {
        // lazy initializer
        if (this.parser == null) {
            InputStream modelIn = null;
            try {
                // Loading the parser model
                //modelIn = getClass().getResourceAsStream("/en-parser-chunker.bin");
                final ParserModel parseModel = new ParserModel(new File("/home/kinow/nlp/models/parser/en-parser-chunking.bin"));
                //modelIn.close();

                this.parser = ParserFactory.create(parseModel);
            } catch (final IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (modelIn != null) {
                    try {
                        modelIn.close();
                    } catch (final IOException e) {
                    } // oh well!
                }
            }
        }
        return this.parser.parse(p);
    }

    public DiscourseEntity[] findEntityMentions(final String[] sentences,
            final String[][] tokens) {
        // tokens should correspond to sentences
        assert (sentences.length == tokens.length);

        // list of document mentions
        final List<Mention> document = new ArrayList<Mention>();

        for (int i = 0; i < sentences.length; i++) {
            // generate the sentence parse tree
            final Parse parse = parseSentence(sentences[i]);

            final DefaultParse parseWrapper = new DefaultParse(parse, i);
            final Mention[] extents = linker.getMentionFinder().getMentions(
                    parseWrapper);

            // Note: taken from TreebankParser source...
            for (int ei = 0, en = extents.length; ei < en; ei++) {
                // construct parses for mentions which don't have constituents
                if (extents[ei].getParse() == null) {
                    // not sure how to get head index, but it doesn't seem to be
                    // used at this point
                    final Parse snp = new Parse(parse.getText(),
                            extents[ei].getSpan(), "NML", 1.0, 0);
                    parse.insert(snp);
                    // setting a new Parse for the current extent
                    extents[ei].setParse(new DefaultParse(snp, i));
                }
            }
            document.addAll(Arrays.asList(extents));
        }

        if (!document.isEmpty()) {
            return linker.getEntities(document.toArray(new Mention[0]));
        }
        return new DiscourseEntity[0];
    }

}
