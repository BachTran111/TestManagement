//package app.NganHangDe.Service;
//
//import com.theokanning.openai.completion.CompletionRequest;
//import com.theokanning.openai.OpenAiService;
//
//public class AIService {
//    private OpenAiService openAiService;
//
//    public AIService(String apiKey) {
//        this.openAiService = new OpenAiService(apiKey);
//    }
//
//    public String suggestAnswer(String question) {
//        String prompt = "Hãy đưa ra đáp án cho câu hỏi sau:\n" + question;
//
//        return openAiService.createCompletion(
//                new CompletionRequest()
//                        .setModel("text-davinci-003")
//                        .setPrompt(prompt)
//                        .setMaxTokens(500)
//        ).getChoices().get(0).getText();
//    }
//}