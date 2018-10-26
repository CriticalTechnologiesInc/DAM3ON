package core;

import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * This uses only the standard library. Not sure how it works
 * on anything other than 'flat' json though.
 * 
 * Converts JSON --> Map<String,?>
 * 
 * @author Jeremy
 *
 */

public class JsonParser {
	private ScriptEngine engine;
	private String json_string;

	public JsonParser() {
		initEngine();
	}

	private void initEngine() {
		ScriptEngineManager sem = new ScriptEngineManager();
		this.engine = sem.getEngineByName("javascript");
	}

	@SuppressWarnings("unchecked")
	public Map<String, ?> parseJson(String input) {
		String json = new String(input);
		this.json_string = json;
		String script = "Java.asJSONCompatible(" + json + ")";
		Object result = null;
		try {
			result = this.engine.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
			return null;
		}

		return (Map<String, ?>) result;
	}

	public String toString(){
		return json_string;
	}
}
