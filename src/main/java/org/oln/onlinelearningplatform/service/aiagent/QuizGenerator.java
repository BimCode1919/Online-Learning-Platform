package org.oln.onlinelearningplatform.service.aiagent;

import dev.langchain4j.service.UserMessage;

public interface QuizGenerator {
    @UserMessage("""
        Dựa trên nội dung sau: {{content}}
        Hãy tạo 5 câu hỏi trắc nghiệm. 
        Bắt buộc trả về định dạng JSON thuần túy như sau (không được bao ngoài bởi ```json):
        {
          "questions": [
            {
              "questionText": "nội dung câu hỏi",
              "explanation": "giải thích",
              "options": [
                {"optionText": "đáp án A", "correct": true},
                {"optionText": "đáp án B", "correct": false}
              ]
            }
          ]
        }
        """)
    String generate(String content);
}
