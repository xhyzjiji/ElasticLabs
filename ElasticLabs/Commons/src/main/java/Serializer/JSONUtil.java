package Serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvfei on 2019/9/9.
 */
public class JSONUtil {
	private static final Logger LOG = LoggerFactory.getLogger(JSONUtil.class);

	public static final String JSON_SERIALIZATION_FAILURE_STRING = "JSON serialization failed";

	public static final ObjectMapper mapper;
	private static final JsonFactory JSON_FACTORY;

	static {
		mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); //解析器支持解析单引号
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true); //解析器支持解析结束符
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 遇到空字段不异常
		JSON_FACTORY = new JsonFactory(mapper);
	}

	/**
	 * Serializes a bean as JSON
	 *
	 * @param <T>
	 * 		the bean type
	 * @param bean
	 * 		the bean to serialize
	 * @param pretty
	 * 		a flag if the output is to be pretty printed
	 * @return the JSON string
	 */
	// TODO: 没必要用泛型，更改signature为toJsonString(Object bean, boolean pretty) throws IOException
	public static <T> String toJsonString(T bean, boolean pretty) throws IOException {
		StringWriter out = new StringWriter(1000);
		JsonGenerator jsonGenerator = JSON_FACTORY.createJsonGenerator(out);
		if (pretty) {
			jsonGenerator.useDefaultPrettyPrinter();
		}
		jsonGenerator.writeObject(bean);
		out.flush();

		return out.toString();
	}

	/**
	 * 一般只在log等不重要场景用
	 */
	public static <T> String toJsonStringIgnoreException(T bean)  {
		try {
			return mapper.writeValueAsString(bean);
		} catch (Exception e) {
			LOG.error("to json {} error", bean, e);
		}
		return null;
	}

	/**
	 * Serializes a bean as JSON. This method will not throw an exception if the serialization fails
	 * but it will instead return the string {@link #JSON_SERIALIZATION_FAILURE_STRING}
	 *
	 * @param <T>
	 * 		the bean type
	 * @param bean
	 * 		the bean to serialize
	 * @param pretty
	 * 		a flag if the output is to be pretty printed
	 * @return the JSON string or {@link #JSON_SERIALIZATION_FAILURE_STRING} if serialization
	 * fails
	 */
	public static <T> String toJsonStringSilent(T bean, boolean pretty) {
		try {
			return toJsonString(bean, pretty);
		} catch (IOException ioe) {
			return JSON_SERIALIZATION_FAILURE_STRING;
		}
	}

	public static <T> Optional<T> jsonToSimpleBean(String jsonStr, Class<T> clazz) {
		if (StringUtils.isBlank(jsonStr)) {
			return Optional.empty();
		}
		try {
			return Optional.of(mapper.readValue(jsonStr, clazz));
		} catch (Exception ioe) {
			LOG.error("parse json {} error", jsonStr, ioe);
			return Optional.empty();
		}
	}
	public static <T> Optional<T> simpleBeanCopy(T bean) {
		try {
			String s = toJsonString(bean, false);
			return Optional.of((T)mapper.readValue(s, bean.getClass()));
		} catch (Exception ioe) {
			LOG.error("simpleBeanCopy  {} error",bean , ioe);
			return Optional.empty();
		}
	}

	public static <T> Optional<T> jsonToBean(String jsonStr, TypeReference<T> typeReference) {
		if (StringUtils.isBlank(jsonStr)) {
			return Optional.empty();
		}
		try {
			return Optional.of(mapper.readValue(jsonStr, typeReference));
		} catch (Exception ioe) {
			LOG.info("parse json {} error", jsonStr, ioe);
			return Optional.empty();
		}
	}

	/**
	 * 仅遍历深度为1的层级，输出map
	 *
	 * @param jsonStr
	 * @return
	 */
	public static Optional<Map<String, String>> jsonToSuperficialMap(String jsonStr) {
		Map<String, Object> map = new HashMap<>();

		try {
			/*JsonFactory jfactory = new JsonFactory();
			JsonParser jParser = jfactory.createJsonParser(jsonStr); // stream? high performance? try it later */
			Map<String, String> ans = new LinkedHashMap<>();
			JsonNode rootNode = mapper.readTree(jsonStr);
			Iterator<Map.Entry<String, JsonNode>> nodesEntry = rootNode.fields();
			while (nodesEntry.hasNext()) {
				Map.Entry<String, JsonNode> node = nodesEntry.next();
				String key = node.getKey();
				JsonNode value = node.getValue();
				if (value.isNull()) {
					ans.put(key, null);
				} else if (value.isContainerNode() || value.isNumber()) {
					ans.put(key, value.toString());
				} else {
					ans.put(key, value.textValue());
				}
			}

			return Optional.of(ans);
		} catch (IOException ioe) {
			return Optional.empty();
		}
	}

	/**
	 * just for fun, 复杂json扁平化输出
	 * input:
	 * {
	 * "a":"A",
	 * "b":{
	 * "b1":"B1",
	 * "b2":"B2"
	 * },
	 * "c":["C1","C2"],
	 * "d":[
	 * {
	 * "d1":"D1",
	 * "d2":"D2"
	 * },
	 * {
	 * "dd1":"DD1",
	 * "dd2":"DD2"
	 * }
	 * ],
	 * "e":{
	 * "e1":["ee","eee"],
	 * "e2":{
	 * "ee1":"EE1",
	 * "ee2":"EE2"
	 * }
	 * }
	 * }
	 * output:
	 * {a=A, b.b1=B1, b.b2=B2, c[0]=C1, c[1]=C2, d[0].d1=D1, d[0].d2=D2, d[1].dd1=DD1, d[1].dd2=DD2, e.e1[0]=ee, e.e1[1]=eee, e.e2.ee1=EE1, e.e2.ee2=EE2}
	 *
	 * @param nodeL0Key
	 * @param nodeL0
	 * @param ans
	 */
	private static void jsonVistor(String nodeL0Key, JsonNode nodeL0, Map<String, String> ans) {
		if (nodeL0.isNull()) {
			ans.put(nodeL0Key, null);
			return;
		}

		if (nodeL0.isValueNode()) {
			ans.put(nodeL0Key, nodeL0.textValue());
			return;
		}

		if (nodeL0.isArray()) {
			Iterator<JsonNode> nodesL1 = nodeL0.iterator();
			int index = 0;
			while (nodesL1.hasNext()) {
				String nodeL1Key = nodeL0Key + "[" + index + "]";
				JsonNode nodeL1 = nodesL1.next();
				if (nodeL1.isValueNode()) {
					ans.put(nodeL1Key, nodeL1.textValue());
				} else {
					jsonVistor(nodeL1Key, nodeL1, ans);
				}
				index++;
			}
		}

		Iterator<Map.Entry<String, JsonNode>> nodesL1 = nodeL0.fields();
		while (nodesL1.hasNext()) {
			Map.Entry<String, JsonNode> nodeL1Entry = nodesL1.next();
			String nodeL1Key = StringUtils.isBlank(nodeL0Key) ? nodeL1Entry.getKey() :
					nodeL0Key + "." + nodeL1Entry.getKey();
			JsonNode nodeL1 = nodeL1Entry.getValue();

			if (nodeL1.isObject()) {
				Iterator<Map.Entry<String, JsonNode>> nodesL2 = nodeL1.fields();
				while (nodesL2.hasNext()) {
					Map.Entry<String, JsonNode> nodeL2Entry = nodesL2.next();
					String nodeL2Key = nodeL2Entry.getKey();
					JsonNode nodeL2 = nodeL2Entry.getValue();
					jsonVistor(nodeL1Key + "." + nodeL2Key, nodeL2, ans);
				}
			} else {
				jsonVistor(nodeL1Key, nodeL1, ans);
			}
		}
	}

	public static String jsonNodeToString(JsonNode jsonNode) throws Exception {
		return mapper.writeValueAsString(jsonNode);
	}

	public static <T> JsonNode beanToJsonNodeSilent(T object) throws Exception {
		String objectStr = mapper.writeValueAsString(object);
		return mapper.readTree(objectStr);
	}

	public static JsonNode stringToJsonNode(String str) throws Exception {
		return mapper.readTree(str);
	}

//	public static Result<ArrayNode> stringToArrayNode(String str) {
//		Result<ArrayNode> res = new Result<>();
//		try {
//			JsonNode jsonNode = mapper.readTree(str);
//			if (jsonNode.isArray()) {
//				ArrayNode arrayNode = mapper.createArrayNode();
//				for (final JsonNode node : jsonNode) {
//					arrayNode.add(node);
//				}
//				res.successData(arrayNode);
//			} else {
//				res.failure("param is not in array node format: " + str);
//			}
//		} catch (Exception e) {
//			LOG.error("transfer {} to array node error", str, e);
//			res.failure("transfer str to array node error: " + e.getMessage());
//		}
//		return res;
//	}

	public static ArrayNode packJsonNodes(JsonNode[] jsonNodes) {
		ArrayNode arrayNode = mapper.createArrayNode();
		for (JsonNode jsonNode : jsonNodes) {
			arrayNode.add(jsonNode);
		}
		return arrayNode;
	}

//	public static Result<String> packJsonNodesIntoArrayAsString(JsonNode[] jsonNodes) {
//		Result<String> res = new Result<>();
//		try {
//			ArrayNode arrayNode = packJsonNodes(jsonNodes);
//			res.successData(mapper.writeValueAsString(arrayNode));
//		} catch (Exception e) {
//			LOG.error("pack json nodes as string error", e);
//			res.failure("pack json nodes as string error: " + e.getMessage());
//		}
//		return res;
//	}
//
//	public static Result<String> packJsonNodeMapAsString(Map<String, JsonNode> jsonNodeMap) {
//		Result<String> res = new Result<>();
//		try {
//			ObjectNode objectNode = mapper.createObjectNode();
//			for (Map.Entry<String, JsonNode> jsonNodeEntry : jsonNodeMap.entrySet()) {
//				objectNode.set(
//					jsonNodeEntry.getKey(),
//					jsonNodeEntry.getValue()
//				);
//			}
//			res.successData(mapper.writeValueAsString(objectNode));
//		} catch (Exception e) {
//			// TODO: 怎么打印jsonNodeMap
//			LOG.error("pack json nodes {} as string error", jsonNodeMap.toString(), e);
//			res.failure("pack json nodes as string error:" + e.getMessage());
//		}
//		return res;
//	}

	public static ArrayNode createArrayNode() {
		return mapper.createArrayNode();
	}

}
