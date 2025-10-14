//package com.air.aiagent.utils;
//import okhttp3.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class RAGFlowClient {
//
//    private static final String API_BASE_URL = "http://localhost/api/v1"; // 替换为实际地址
//    private static final String API_KEY = "Bearer ragflow-VjZWQyY2Q4OWViYzExZjBiZmJhYmVmYW"; // 替换为你的API Key
//    private static final OkHttpClient client = new OkHttpClient();
//
//    public static void main(String[] args) {
//        // 1. 创建数据集
//        String datasetId ="";//数据集id
//        //datasetId = createDataset("my_dataset"+ UuidUtils.getUUID());
//        List<String> filePathsList=new ArrayList<>();
//        filePathsList.add("F:\\111\\gu21antou.pdf");
//        datasetId="ee46dcaa19bf11f080d40242ac120006";
//        //filePathsList.add("F:\\111\\2.txt");
//        // 2. 上传文档加入知识库必须用
//        //List<String> documentIds = uploadDocuments(datasetId, filePathsList);
//        // 3. 解析文档加入知识库必须用
//        //parseDocuments(datasetId, documentIds);
//        //// 4. 检索内容可不用
//        //String answerStr= retrieveChunks("ee46dcaa19bf11f080d40242ac120006", "招标项目名称");
//        // System.out.println("answerStr"+answerStr);
//        //5.创建聊天助手
//        String chatId = ""; //聊天助手id
//        chatId = createChat(datasetId); // 5. 创建会话
//
//        //6. 创建会话
//        String sessionId = createChatSession(chatId, "my_session"); //会话id
//        // 7. 对话交互(需要等文件解析完)
//        String answerStr= chatCompletion(chatId, sessionId, "交货地点是多少");
//        System.out.println("answerStr:"+answerStr);
//    }
//
//    // 1. 创建数据集
//    // 1. 创建数据集
//    public static String createDataset(String datasetName) {
//        JSONObject requestBody = new JSONObject().put("name", datasetName);
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/datasets")
//                .post(RequestBody.create( MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//        String datasetId ="";
//        try (Response response = client.newCall(request).execute()) {
//            if (response.isSuccessful() && response.body() != null) {
//
//                try {
//                    // 将响应体转换为字符串
//                    String responseBody = response.body().string();
//                    // 创建 JSONObject 对象
//                    JSONObject responseData = new JSONObject(responseBody);
//
//                    // 检查是否存在 "data" 字段
//                    if (responseData.has("data") && !responseData.isNull("data")) {
//                        // 获取 "data" 对象
//                        JSONObject data = responseData.getJSONObject("data");
//
//                        // 检查 "data" 对象中是否存在 "id" 字段
//                        if (data.has("id")) {
//                            // 获取 "id" 字段的值
//                            datasetId = data.getString("id");
//                            // 打印数据集 ID
//                            System.out.println("数据集创建成功，ID: " + datasetId);
//                        } else {
//                            // 如果 "data" 对象中没有 "id" 字段
//                            System.out.println("响应数据中未找到 'id' 字段。响应内容: " + responseBody);
//                        }
//                    } else {
//                        // 如果没有 "data" 字段
//                        System.out.println("响应数据中未找到 'data' 字段。响应内容: " + responseBody);
//                    }
//                } catch (Exception e) {
//                    // 捕获异常，例如 JSON 格式错误或字段不存在
//                    System.out.println("解析 JSON 数据时发生错误: " + e.getMessage());
//                }
//            } else {
//                // 如果响应无效或没有响应体
//                System.out.println("请求失败，状态码: " + response.code());
//            }
//            System.out.println("数据集创建成功，ID: " + datasetId);
//            return datasetId;
//        } catch (IOException e) {
//            throw new RuntimeException("创建数据集失败: " + e.getMessage());
//        }
//    }
//
//    // 2. 上传文档（支持多文件）
//    public static List<String> uploadDocuments(String datasetId, List<String> filePaths) {
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM);
//
//        for (String filePath : filePaths) {
//            File file = new File(filePath);
//            builder.addFormDataPart("file", file.getName(),
//                    RequestBody.create( MediaType.parse("application/octet-stream"),file));
//        }
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/datasets/" + datasetId + "/documents")
//                .post(builder.build())
//                .header("Authorization", API_KEY)
//                .build();
//        List<String> result = new ArrayList<>();
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body().string();
//            // 1. 解析整个 JSON
//            JSONObject responseData = new JSONObject(responseBody);
//
//            // 2. 获取 "data" 数组（注意：这里是一个 JSONArray，不是 JSONObject）
//            JSONArray dataArray = responseData.getJSONArray("data");
//
//            // 3. 获取 data 数组的第一个元素（JSONObject）
//            JSONObject firstDataItem = dataArray.getJSONObject(0);
//
//            // 4. 获取 "id" 的值
//            String documentId = firstDataItem.getString("id");
//            result.add(documentId); // 或 documents.get(i).toString()
//            return result;
//            // return documents.toList().stream().map(Object::toString).toList();
//        } catch (IOException e) {
//            throw new RuntimeException("上传文档失败: " + e.getMessage());
//        }
//    }
//
//
//    // 3. 解析文档
//    public static void parseDocuments(String datasetId, List<String> documentIds) {
//        JSONObject requestBody = new JSONObject()
//                .put("document_ids", new JSONArray(documentIds));
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/datasets/" + datasetId + "/chunks")
//                .post(RequestBody.create( MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            System.out.println("文档解析结果: " + response.body().string());
//        } catch (IOException e) {
//            throw new RuntimeException("解析文档失败: " + e.getMessage());
//        }
//    }
//
//
//    // 4. 检索内容
//    public static String retrieveChunks(String datasetId, String question) {
//        JSONObject requestBody = new JSONObject()
//                .put("question", question)
//                .put("dataset_ids", new JSONArray().put(datasetId));
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/retrieval")
//                .post(RequestBody.create(MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//        String answerStr="";
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body().string();
//
//            try {
//                // 1. 解析整个 JSON
//                JSONObject responseData = new JSONObject(responseBody);
//
//                // 2. 获取 "data" 对象（注意：这里是一个 JSONObject，不是 JSONArray）
//                JSONObject data = responseData.getJSONObject("data");
//
//                // 3. 获取 "chunks" 数组
//                JSONArray chunks = data.getJSONArray("chunks");
//
//                // 4. 检查 chunks 是否为空
//                if (chunks.length() == 0) {
//                    System.out.println("chunks 是空的，没有 content_ltks");
//                }
//
//                // 5. 获取 chunks 的第一个元素（JSONObject）
//                JSONObject firstChunk = chunks.getJSONObject(0);
//
//                // 6. 获取 "content_ltks" 的值
//                String contentLtks = firstChunk.getString("content_ltks");
//
//                System.out.println("content_ltks: " + contentLtks);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
////            String contentLtks ="";
////            try {
////                JSONObject result = new JSONObject(response.body());
////                if (result.has("data") && !result.isNull("data")) {
//////                    JSONObject data = result.getJSONObject("data");
//////                    if (data != null) {
//////                        JSONArray chunks = data.getJSONArray("chunks");
//////
//////                        // 判断 chunks 是否为空
//////                        if (chunks.length() == 0) {
//////                            System.out.println("chunks 是空的，没有 content_ltks");
//////                            contentLtks = "";
//////                        }
//////
//////                        // 非空时正常处理
//////                        JSONObject firstChunk = chunks.getJSONObject(0);
//////                        contentLtks = firstChunk.getString("content_ltks");
//////                        System.out.println("content_ltks: " + contentLtks);
//////                    }
////                }
//
//        } catch (IOException e) {
//            throw new RuntimeException("检索失败: " + e.getMessage());
//        }
//        return answerStr;
//    }
//
//
//    // 5.0. 创建聊天助手
//    public static String createChat(String dataset_ids) {
//        JSONObject requestBody = new JSONObject().put("dataset_ids", new JSONArray().put(dataset_ids)).put("name", "Chatname"+ UuidUtils.getUUID());
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/chats")
//                .post(RequestBody.create(MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body().string();
//            // 1. 解析JSON字符串
//            JSONObject responseData = new JSONObject(responseBody);
//
//            // 2. 获取"data"对象
//            JSONObject data = responseData.getJSONObject("data");
//
//            // 3. 直接获取"id"字段（注意：不是数组，是字符串）
//            String chatId = data.getString("id");
//            System.out.println("聊天助手创建成功，ID: " + chatId);
//            return chatId;
//        } catch (IOException e) {
//            throw new RuntimeException("聊天助手创建失败: " + e.getMessage());
//        }
//    }
//
//
//    // 5. 创建聊天会话
//    public static String createChatSession(String chatId, String sessionName) {
//        sessionName=UuidUtils.getUUID();
//        JSONObject requestBody = new JSONObject().put("name", sessionName);
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/chats/" + chatId + "/sessions")
//                .post(RequestBody.create(MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body().string();
//            // 1. 解析JSON字符串
//            JSONObject responseData = new JSONObject(responseBody);
//
//            // 2. 获取"data"对象
//            JSONObject data = responseData.getJSONObject("data");
//
//            // 3. 直接获取"id"字段（注意：不是数组，是字符串）
//            String sessionId = data.getString("id");
//            System.out.println("会话创建成功，ID: " + sessionId);
//            return sessionId;
//        } catch (IOException e) {
//            throw new RuntimeException("创建会话失败: " + e.getMessage());
//        }
//    }
//
//
//    // 6. 与聊天助手对话，实际问问题
//    public static String chatCompletion(String chatId, String sessionId, String question) {
//        JSONObject requestBody = new JSONObject()
//                .put("session_id", sessionId)
//                .put("stream", true)
//                .put("question", question);
//
//        Request request = new Request.Builder()
//                .url(API_BASE_URL + "/chats/" + chatId + "/completions")
//                .post(RequestBody.create(MediaType.parse("application/json"),requestBody.toString()))
//                .header("Authorization", API_KEY)
//                .build();
//        String answer="";
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body().string();
//            System.out.println("responseBody结果: " + responseBody);
//            // 1. 解析JSON字符串
//            JSONObject responseData = new JSONObject(responseBody);
//
//            // 2. 获取"data"对象
//            JSONObject data = responseData.getJSONObject("data");
//
//            // 3. 直接获取"answer"字段（注意：不是数组，是字符串）
//            answer = data.getString("answer");
//            System.out.println("AI回复: " + answer);
//        } catch (IOException e) {
//            throw new RuntimeException("对话失败: " + e.getMessage());
//        }
//        return answer;
//    }
//}