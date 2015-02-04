package opennlp;

import java.io.File;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Tags01 {

    public static void main(String[] args) throws Exception {

        SentenceModel sdModel = new SentenceModel(new File("/home/kinow/nlp/models/opennlp/pt-sent.model"));
        TokenizerModel tkModel = new TokenizerModel(new File("/home/kinow/nlp/models/opennlp/pt-tok.model"));
        POSModel posModel = new POSModel(new File("/home/kinow/nlp/models/opennlp/pt-pos.model"));

        SentenceDetector sentenceDetector = new SentenceDetectorME(sdModel);
        Tokenizer tokenizer = new TokenizerME(tkModel);
        POSTagger tagger = new POSTaggerME(posModel);

        String question = "Quantas pessoas recebem o bolsa família no estado do Acre? Foi o que ele perguntou...";

        String[] sentences = sentenceDetector.sentDetect(question);

        System.out.println(sentences[0]);

        String sentence = sentences[0];

        String[] tokens = tokenizer.tokenize(sentence);

        String[] tags = tagger.tag(tokens);

        for (int i = 0; i < tokens.length; i++) {
            System.out.println(String.format("[%s]=>[%s]\t", tokens[i], tags[i]));
        }
    }

}
