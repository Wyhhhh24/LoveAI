package com.air.aiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Re2 Advisor
 * 可提高大预言模型的推理能力
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	/**
	 * 执行请求前，改写prompt
	 */
	private AdvisedRequest before(AdvisedRequest advisedRequest) { 

		//我们拿到了现在的Request对象，这里需要构造新的request对象
		Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
		advisedUserParams.put("re2_input_query", advisedRequest.userText());
		// "re2_input_query" 这里是一个动态变量，将用户的输入动态替换到这个动态变量里面

		//最终构造出一个新的请求发送给大模型
		return AdvisedRequest.from(advisedRequest)
			.userText("""
			    {re2_input_query}
			    Read the question again: {re2_input_query}
			    """)
			.userParams(advisedUserParams)
			.build();
	}

	//这里还是要实现aroundCall接口
	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		//但是这里是不需要对响应进行处理了，可以借鉴一下MyLoggerAdvisor的定义，两者是不同的，这里只在执行请求前做处理，没有对响应进行处理
		return chain.nextAroundCall(this.before(advisedRequest));
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		return chain.nextAroundStream(this.before(advisedRequest));
	}

	@Override
	public int getOrder() { 
		return 0;
	}

    @Override
    public String getName() { 
		return this.getClass().getSimpleName();
	}
}