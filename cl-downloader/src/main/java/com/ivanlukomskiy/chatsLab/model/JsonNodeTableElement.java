package com.ivanlukomskiy.chatsLab.model;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.JsonNode;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 22.10.2018.
 */
public class JsonNodeTableElement implements ChatTableElement {

    @Getter
    private final JsonNode node;

    @Getter
    @Setter
    private boolean download = false;

    public JsonNodeTableElement(JsonNode node) {
        this.node = node;
    }

    @Override
    public String getName() {
        return node.get("name").asText();
    }
}
