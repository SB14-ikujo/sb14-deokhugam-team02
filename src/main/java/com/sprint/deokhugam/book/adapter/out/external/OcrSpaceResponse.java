package com.sprint.deokhugam.book.adapter.out.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OcrSpaceResponse(
    @JsonProperty("ParsedResults") List<ParsedResult> parsedResults,
    @JsonProperty("IsErroredOnProcessing") boolean erroredOnProcessing,
    @JsonProperty("ErrorMessage") Object errorMessage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParsedResult(
        @JsonProperty("ParsedText") String parsedText
    ) {
    }
}
