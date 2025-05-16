package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApikey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper){
        this.webClient=webClientBuilder.build();
        this.objectMapper=objectMapper;
    }
    public String processContent(ResearchRequest request) {
        String prompt = buildPrompt(request);//build prompt
//query the ai model api
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApikey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        return extractTextFromResponse(response);
    }
    private String extractTextFromResponse(String response){

            try
            {
GeminiResponse geminiResponse= objectMapper.readValue(response, GeminiResponse.class);
if(geminiResponse.getCandidates()!=null && !geminiResponse.getCandidates().isEmpty()){
    GeminiResponse.Candidate firstCandidate= geminiResponse.getCandidates().get(0);
    if(firstCandidate.getContent()!=null && firstCandidate.getContent().getParts()!= null && !firstCandidate.getContent().getParts().isEmpty()){
        return firstCandidate.getContent().getParts().get(0).getText();
    }
}return "no content found";

            }
            catch(Exception e)
            {
               return "error parsing: " + e.getMessage();
            }
        }

    private String buildPrompt (ResearchRequest request ){
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()){
            case "summarize":
                prompt.append("provide a clear and concise summary of the following text in a few sentences:\n\n");
                break;
            case "suggest":
                prompt.append("based on the following content:suggest related topics and readings. format the response with clear headings and bullet points:\n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}
