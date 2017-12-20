package semantics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import java.io.IOException;
import java.util.List;

public class DocumentFeatures {
    public List<String> ngrams;
    public List<String> partsOfSpeech;
    public Sentiment sentiment;
    public List<NamedEntity> entities;
    public List<SpoTuple> relations;
    public Double avgWordLength;
    public Double avgWordsPerSentence;
    public Integer numSentences;
    public Integer uniqueWordsCount;
    public Boolean containsQuotes;
    public Boolean containsIncompleteQuotes;
    public List<TfIdfTerm> tfIdf;
    public double[] word2Vec;
    public double[] gloVe;

    /**
     * Creates a JSON representation of the object.
     * Note: its performance is not tested and still
     * questionable.
     * @return An object node to the root JSON object.
     */
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper.valueToTree(this);
    }

    public ObjectNode toJsonMock() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }

    /**
     * Creates a MsgPack representation of the object.
     * Note: its performance is not tested and still
     * questionable.
     * @return A byte array of the MsgPack
     */
    public byte[] toMsgpackBytes() {
        ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        try {
            return mapper.writeValueAsBytes(this);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    /**
     * Deserializes a JSON object into an instance
     * of DocumentsFeatures.
     * @param object The JSON object to deserialize.
     * @return The corresponding instance of DocumentsFeatures.
     */
    public static DocumentFeatures fromJson(ObjectNode object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.treeToValue(object, DocumentFeatures.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializes a MsgPack byte array into an instance
     * of DocumentsFeatures.
     * @param buffer The bytes of the pack to deserialize.
     * @return The corresponding instance of DocumentsFeatures.
     */
    public static DocumentFeatures fromMsgPack(byte[] buffer) {
        ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
        try {
            return mapper.readValue(buffer, DocumentFeatures.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
} 
