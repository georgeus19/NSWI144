
public class Utils {	
	public static String CreateLiteralProperty(String property) {
		return " property=\"" + property + "\"";
	}
	
	public static String CreatePropertyToEntity(String property, String entityURI) {
		return " rel=\"" + property + " href=\"" + entityURI + "\" ";
	}
	
	public static String CreatePropertyToEntity(String property) {
		return " rel=\"" + property + "\" ";
	}
	
	public static String CreateDataType(String datatype, String content) {
		String s = " datatype=\"" + datatype + "\" ";
		if (content != null) {
			s = s + " content\"" + content + "\" ";
		}
		return s;
	}
	
	public static String CreateClassType(String type) {
		return " typeof=\"" + type + "\" ";
	}
	
	public static String CreateSubject(String subject) {
		return " about=" + subject + " ";
	}
	
	public static String CreateObjectAndSubject(String object) {
		return " resource=" + object + " ";
	}
	
	public static String CreateMenu() {
		return "<ul>"
				+ "<li><a href=\"http://localhost:8080/nswi144-births-regions/CzechRegionsCbr\">Relativni porodnost v CR</a></li>"
				+ "<li><a href=\"http://localhost:8080/nswi144-births-regions/CzechRegionsByBirthsServlet?min=1500&max=3000\">CR porodnost s odkazi na UK regiony</a></li>"
				+ "</ul>";
	}
	
	
}
