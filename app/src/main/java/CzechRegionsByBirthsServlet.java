

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Servlet implementation class CzechRegionServlet
 */
public class CzechRegionsByBirthsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CzechRegionsByBirthsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Repository repo = new SPARQLRepository("http://localhost:8890/sparql/");
		repo.init();
		
		String max = request.getParameter("max");
		String min = request.getParameter("min");
		if (max == null) {
			max = "2000";
		}
		if (min == null) {
			min = "1000";
		}

		String queryString = "PREFIX xsd:  <https://www.w3.org/2001/XMLSchema#> \n"
				+ "PREFIX skos:  <http://www.w3.org/2004/02/skos/core#> \n"
				+ "\n"
				+ "# Get regions with live births between 1000 and 2000.\n"
				+ "SELECT ?czechBirthObs ?czechBirthRegion ?regionName ?liveBirths\n"
				+ "WHERE {\n"
				+ "?czechBirthObs <http://example.org/opendata/czech/births/dimension/refArea> ?czechBirthRegion ;\n"
				+ "    <http://example.org/opendata/czech/births/measure/liveBirths> ?liveBirths .\n"
				+ "\n"
				+ "?czechBirthRegion skos:prefLabel ?regionName .\n"
				+ "\n"
				+ "FILTER(xsd:integer(?liveBirths) < " + max + ")\n"
				+ "FILTER(xsd:integer(?liveBirths) > "+ min + ")\n"
				+ "}\n"
				+ "";
		
		response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
		response.getWriter().println("<html lang=cs>");
		response.getWriter().println("<style>\n"
				+ "table, th, td {\n"
				+ "  border:1px solid black;\n"
				+ "}\n"
				+ "</style>");

		response.getWriter().println("<body prefix=\"xsd: https://www.w3.org/2001/XMLSchema#\"><div prefix=\"skos: http://www.w3.org/2004/02/skos/core#\">");
		response.getWriter().println(Utils.CreateMenu());
		response.getWriter().println("<h1>Pocty narozeni v Cesku za rok 2021</h1><table>");
		response.getWriter().println("<tr><th>Id Pozorovani</th><th>Region</th><th>Narozeno za rok 2021</th></tr>");
		
		try (RepositoryConnection con = repo.getConnection()) {
		    TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		    TupleQueryResult result = tupleQuery.evaluate();
		   
		    List<BindingSet> bindings = result.stream().toList();
		    for (BindingSet binding : bindings) {
		    	Value obs = binding.getValue("czechBirthObs");

		    	response.getWriter().println("<tr" + Utils.CreateSubject(obs.stringValue() + ">"));
		        String[] uriParts = obs.stringValue().split("/");
		        response.getWriter().print("<td>" + uriParts[uriParts.length - 1] + "</td>");
		        		        
		        Value region = binding.getValue("czechBirthRegion");
		        Value regionName = binding.getValue("regionName");
		        response.getWriter().print("<td" + Utils.CreateObjectAndSubject(region.stringValue()));
		        response.getWriter().print(Utils.CreatePropertyToEntity("http://example.org/opendata/czech/births/dimension/refArea") + ">");
		        response.getWriter().print("<span" + Utils.CreateLiteralProperty("skos:prefLabel") + ">");
		        response.getWriter().print("<a href=http://localhost:8080/nswi144-births-regions/CzechRegionServlet?obs=" + uriParts[uriParts.length - 1] + ">");
		        response.getWriter().print(regionName.stringValue() + "</a></span></td>");
		 
		        Value liveBirths = binding.getValue("liveBirths");
		        response.getWriter().print(
	        		"<td" + Utils.CreateLiteralProperty("http://example.org/opendata/czech/births/measure/liveBirths") 
         			+ Utils.CreateDataType("xsd:integer", null) + ">");
		        response.getWriter().print(liveBirths.stringValue() + "</td>");
		        response.getWriter().println("</tr>");
		    }
		}
		response.getWriter().print("</table></div></body></html>");
	}

}
