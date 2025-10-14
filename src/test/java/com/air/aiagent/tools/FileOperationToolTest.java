package com.air.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "air.txt";
        String result = tool.readFile(fileName);
        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "air.txt";
        String content = "知识即是力量ol";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }
}